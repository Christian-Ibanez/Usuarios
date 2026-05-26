package com.SanosySalvos.Usuarios.controller;

import com.SanosySalvos.Usuarios.model.RolUsuario;
import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios") 
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<Usuario> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        // Asumiendo que tienes este método en tu UsuarioService
        Usuario usuarioCreado = usuarioService.registrarUsuario(nuevoUsuario);
        return new ResponseEntity<>(usuarioCreado, HttpStatus.CREATED);
    }

    @GetMapping("/correo/{correoElectronico}")
    public ResponseEntity<Usuario> obtenerUsuarioPorCorreo(@PathVariable String correoElectronico) {
        Usuario usuario = usuarioService.obtenerUsuarioPorCorreo(correoElectronico);
        // Retornamos 200 (OK) con los datos del usuario
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/instituciones/pendientes")
    public ResponseEntity<List<Usuario>> obtenerInstitucionesPendientes() {
        List<Usuario> pendientes = usuarioService.obtenerInstitucionesPendientes();
        return ResponseEntity.ok(pendientes);
    }

    @PutMapping("/instituciones/{id}/aprobar")
    public ResponseEntity<Usuario> aprobarCuentaInstitucional(@PathVariable Long id) {
        Usuario usuarioAprobado = usuarioService.aprobarCuentaInstitucional(id);
        return ResponseEntity.ok(usuarioAprobado);
    }

    @PutMapping("/{id}/solicitar-rol")
    public ResponseEntity<Usuario> solicitarCambioRol(
            @PathVariable Long id, 
            @RequestParam RolUsuario nuevoRol,
            @RequestParam String urlDocumento) { 
            
        Usuario usuarioActualizado = usuarioService.solicitarCambioRol(id, nuevoRol, urlDocumento);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @GetMapping("/prueba-fallback")
    public ResponseEntity<String> probarFallback() {

        String respuesta = usuarioService.notificarNuevoUsuario("prueba@mail.com");
        return ResponseEntity.ok(respuesta);
    }
}
