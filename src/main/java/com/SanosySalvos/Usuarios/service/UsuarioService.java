package com.SanosySalvos.Usuarios.service;

import com.SanosySalvos.Usuarios.model.RolUsuario;
import com.SanosySalvos.Usuarios.model.Usuario;
import java.util.List;

public interface UsuarioService {
    

    Usuario registrarUsuario(Usuario nuevoUsuario);
    
    Usuario obtenerUsuarioPorCorreo(String correoElectronico);
    
    List<Usuario> obtenerInstitucionesPendientes();
    
    Usuario aprobarCuentaInstitucional(Long usuarioId);

    Usuario solicitarCambioRol(Long usuarioId, RolUsuario nuevoRol, String urlDocumento);

    String notificarNuevoUsuario(String correoElectronico);
}