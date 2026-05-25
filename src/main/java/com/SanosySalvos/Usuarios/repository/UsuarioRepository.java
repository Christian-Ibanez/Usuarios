package com.SanosySalvos.Usuarios.repository;

import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.model.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoElectronico(String correoElectronico);

    boolean existsByCorreoElectronico(String correoElectronico);

    List<Usuario> findByRol(RolUsuario rol);

    List<Usuario> findByRolInAndCuentaValidadaFalse(List<RolUsuario> roles);
}