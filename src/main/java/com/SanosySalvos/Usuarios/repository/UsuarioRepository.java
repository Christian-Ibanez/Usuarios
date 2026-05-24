package com.SanosySalvos.Usuarios.repository;

import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.model.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // 1. Buscar un usuario por su correo (Vital para el Login y generar el token JWT)
    Optional<Usuario> findByCorreoElectronico(String correoElectronico);

    // 2. Verificar si un correo ya existe (Útil antes de registrar para lanzar un error amigable)
    boolean existsByCorreoElectronico(String correoElectronico);

    // 3. Obtener una lista de usuarios según su rol
    List<Usuario> findByRol(RolUsuario rol);

    // 4. Buscar instituciones (ej. Clínicas) que aún no han sido validadas por el Administrador
    List<Usuario> findByRolInAndCuentaValidadaFalse(List<RolUsuario> roles);
}