package com.SanosySalvos.Usuarios.controller;

import com.SanosySalvos.Usuarios.dto.AuthResponseDTO;
import com.SanosySalvos.Usuarios.dto.LoginRequestDTO;
import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.repository.UsuarioRepository;
import com.SanosySalvos.Usuarios.security.JwtUtil;
import com.SanosySalvos.Usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 1. ENDPOINT DE REGISTRO (Público)
    // POST http://localhost:8080/api/usuarios/registro
    @PostMapping("/registro")
    public ResponseEntity<Usuario> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        Usuario usuarioCreado = usuarioService.registrarUsuario(nuevoUsuario);
        return new ResponseEntity<>(usuarioCreado, HttpStatus.CREATED);
    }

    // 2. ENDPOINT DE LOGIN (Público)
    // POST http://localhost:8080/api/usuarios/login
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {
        
        Usuario usuario = usuarioRepository.findByCorreoElectronico(request.getCorreoElectronico())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String token = jwtUtil.generarToken(usuario.getCorreoElectronico(), usuario.getRol().name());
        
        return new AuthResponseDTO(token);
    }
}