package com.SanosySalvos.Usuarios.service;

import com.SanosySalvos.Usuarios.model.RolUsuario;
import com.SanosySalvos.Usuarios.model.Usuario;
import java.util.List;

public interface UsuarioService {
    
    // Registra un nuevo usuario en la plataforma
    Usuario registrarUsuario(Usuario nuevoUsuario);
    
    // Busca un usuario por su correo (necesario para el login)
    Usuario obtenerUsuarioPorCorreo(String correoElectronico);
    
    // Obtiene instituciones pendientes de validación
    List<Usuario> obtenerInstitucionesPendientes();
    
    // El administrador aprueba una cuenta institucional
    Usuario aprobarCuentaInstitucional(Long usuarioId);

    // Modificamos este método para recibir la URL o ruta del documento
    Usuario solicitarCambioRol(Long usuarioId, RolUsuario nuevoRol, String urlDocumento);

    // Método para probar Resilience4j
    String notificarNuevoUsuario(String correoElectronico);
}