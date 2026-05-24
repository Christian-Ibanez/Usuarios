package com.SanosySalvos.Usuarios.service.impl;

import com.SanosySalvos.Usuarios.model.RolUsuario; // Enum que debes crear
import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.repository.UsuarioRepository;
import com.SanosySalvos.Usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 1. Regla de negocio: Verificar si el correo ya existe
        if (usuarioRepository.existsByCorreoElectronico(nuevoUsuario.getCorreoElectronico())) {
            throw new RuntimeException("Error: El correo electrónico ya está registrado.");
        }

        // 2. Lógica de seguridad: Encriptar la contraseña antes de guardar
        // nuevoUsuario.setContrasena(passwordEncoder.encode(nuevoUsuario.getContrasena()));

        // 3. Lógica por defecto: Si es ciudadano, su cuenta no requiere validación manual
        if (nuevoUsuario.getRol() == RolUsuario.CIUDADANO) {
            nuevoUsuario.setCuentaValidada(true);
        } else {
            nuevoUsuario.setCuentaValidada(false); // Clínicas y refugios esperan aprobación
        }

        // 4. Guardar en PostgreSQL
        return usuarioRepository.save(nuevoUsuario);
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