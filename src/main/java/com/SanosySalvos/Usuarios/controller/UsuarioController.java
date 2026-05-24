package com.SanosySalvos.Usuarios.controller;

import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios") // Ruta base para todos los endpoints de este controlador
@RequiredArgsConstructor
public class UsuarioController {

    // Inyectamos el servicio (la interfaz, NO la implementación). 
    // Spring Boot conectará la implementación automáticamente.
    private final UsuarioService usuarioService;

    // 1. Endpoint para registrar un usuario
    // POST http://localhost:8080/api/usuarios
    @PostMapping
    public ResponseEntity<Usuario> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        Usuario usuarioCreado = usuarioService.registrarUsuario(nuevoUsuario);
        // Retornamos 201 (CREATED) porque se creó un nuevo recurso en la base de datos
        return new ResponseEntity<>(usuarioCreado, HttpStatus.CREATED); 
    }

    // 2. Endpoint para buscar un usuario por su correo (Útil para el Login interno)
    // GET http://localhost:8080/api/usuarios/correo/juan@email.com
    @GetMapping("/correo/{correoElectronico}")
    public ResponseEntity<Usuario> obtenerUsuarioPorCorreo(@PathVariable String correoElectronico) {
        Usuario usuario = usuarioService.obtenerUsuarioPorCorreo(correoElectronico);
        // Retornamos 200 (OK) con los datos del usuario
        return ResponseEntity.ok(usuario);
    }

    // 3. Endpoint para que el Administrador vea las veterinarias pendientes
    // GET http://localhost:8080/api/usuarios/instituciones/pendientes
    @GetMapping("/instituciones/pendientes")
    public ResponseEntity<List<Usuario>> obtenerInstitucionesPendientes() {
        List<Usuario> pendientes = usuarioService.obtenerInstitucionesPendientes();
        return ResponseEntity.ok(pendientes);
    }

    // 4. Endpoint para que el Administrador apruebe una cuenta
    // PUT http://localhost:8080/api/usuarios/instituciones/5/aprobar
    @PutMapping("/instituciones/{id}/aprobar")
    public ResponseEntity<Usuario> aprobarCuentaInstitucional(@PathVariable Long id) {
        Usuario usuarioAprobado = usuarioService.aprobarCuentaInstitucional(id);
        return ResponseEntity.ok(usuarioAprobado);
    }
}
