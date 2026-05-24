package com.SanosySalvos.Usuarios.service.impl;

import com.SanosySalvos.Usuarios.model.RolUsuario; // Enum que debes crear
import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.repository.UsuarioRepository;
import com.SanosySalvos.Usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    // Inyección de dependencias a través del constructor (gracias a @RequiredArgsConstructor de Lombok)
    private final UsuarioRepository usuarioRepository;
    
    // Nota: Aquí también inyectarías el PasswordEncoder de Spring Security más adelante
    // private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Usuario registrarUsuario(Usuario nuevoUsuario) {
        if (usuarioRepository.existsByCorreoElectronico(nuevoUsuario.getCorreoElectronico())) {
            throw new RuntimeException("Error: El correo electrónico ya está registrado.");
        }

        // REGLA 1: Todo usuario nuevo nace estrictamente como CIUDADANO y validado
        nuevoUsuario.setRol(RolUsuario.CIUDADANO);
        nuevoUsuario.setCuentaValidada(true); 

        return usuarioRepository.save(nuevoUsuario);
    }

    @Override
    @Transactional
    public Usuario solicitarCambioRol(Long usuarioId, RolUsuario nuevoRol, String urlDocumento) {
        
        List<RolUsuario> rolesPermitidos = List.of(
                RolUsuario.VETERINARIA,
                RolUsuario.REFUGIO,
                RolUsuario.MUNICIPALIDAD
        );

        // Cambiamos el error genérico por un 403 FORBIDDEN
        if (!rolesPermitidos.contains(nuevoRol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: El rol solicitado no es válido para una cuenta institucional.");
        }

        // Cambiamos el error del documento por un 400 BAD REQUEST
        if (urlDocumento == null || urlDocumento.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es obligatorio adjuntar un documento válido.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setRol(nuevoRol);
        usuario.setCuentaValidada(false); 
        usuario.setUrlDocumentoValidacion(urlDocumento); 

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario obtenerUsuarioPorCorreo(String correoElectronico) {
        return usuarioRepository.findByCorreoElectronico(correoElectronico)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correoElectronico));
    }

    @Override
    public List<Usuario> obtenerInstitucionesPendientes() {
        // Retorna clínicas o refugios que aún no han sido validados
        return usuarioRepository.findByRolAndCuentaValidadaFalse(RolUsuario.VETERINARIA); 
    }

    @Override
    @Transactional
    public Usuario aprobarCuentaInstitucional(Long usuarioId) {
        // Buscamos al usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Cambiamos su estado
        usuario.setCuentaValidada(true);
        
        // Guardamos los cambios
        return usuarioRepository.save(usuario);
    }
}