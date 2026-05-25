package com.SanosySalvos.Usuarios.service.impl;

import com.SanosySalvos.Usuarios.model.RolUsuario; // Enum que debes crear
import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.repository.UsuarioRepository;
import com.SanosySalvos.Usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    

    @Override
    @Transactional
    public Usuario registrarUsuario(Usuario nuevoUsuario) {
        if (usuarioRepository.existsByCorreoElectronico(nuevoUsuario.getCorreoElectronico())) {
            throw new RuntimeException("Error: El correo electrónico ya está registrado.");
        }

        nuevoUsuario.setRol(RolUsuario.CIUDADANO);
        nuevoUsuario.setCuentaValidada(true); 

        nuevoUsuario.setContrasena(passwordEncoder.encode(nuevoUsuario.getContrasena()));

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

        if (!rolesPermitidos.contains(nuevoRol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: El rol solicitado no es válido para una cuenta institucional.");
        }

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
        
        List<RolUsuario> rolesInstitucionales = List.of(
                RolUsuario.VETERINARIA,
                RolUsuario.REFUGIO,
                RolUsuario.MUNICIPALIDAD
        );

        return usuarioRepository.findByRolInAndCuentaValidadaFalse(rolesInstitucionales);
    }

    @Override
    @Transactional
    public Usuario aprobarCuentaInstitucional(Long usuarioId) {
    
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setCuentaValidada(true);
        
        return usuarioRepository.save(usuario);
    }

    
    @CircuitBreaker(name = "servicioExterno", fallbackMethod = "notificacionFallback")
    public String notificarNuevoUsuario(String correoElectronico) {
        
        throw new RuntimeException("¡El microservicio de Notificaciones está caído!");
    }

   
    public String notificacionFallback(String correoElectronico, Exception e) {
        System.out.println("No se pudo notificar a " + correoElectronico + ". Razón: " + e.getMessage());
        return "Usuario registrado, pero el correo de bienvenida se enviará más tarde.";
    }
}