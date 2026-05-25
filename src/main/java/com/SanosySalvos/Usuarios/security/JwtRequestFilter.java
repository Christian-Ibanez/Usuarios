package com.SanosySalvos.Usuarios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. Buscamos la cabecera "Authorization" en la petición de Postman/Frontend
        final String authorizationHeader = request.getHeader("Authorization");

        String correo = null;
        String jwt = null;

        // 2. Por convención mundial, los tokens JWT siempre empiezan con la palabra "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Cortamos los primeros 7 caracteres ("Bearer ")
            try {
                correo = jwtUtil.extraerCorreo(jwt); // Usamos tu JwtUtil para desencriptarlo
            } catch (Exception e) {
                System.out.println("Token inválido o manipulado: " + e.getMessage());
            }
        }

        // 3. Si logramos extraer el correo y el usuario aún no ha sido autenticado en este ciclo
        if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. Le preguntamos a JwtUtil si el token no ha expirado matemáticamente
            if (jwtUtil.validarToken(jwt)) {
                
                // 5. ¡Aprobado! Creamos un "Gafete de Visitante" oficial para Spring Security
                // (Por ahora le pasamos un ArrayList vacío en los roles para mantenerlo simple)
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        correo, null, new ArrayList<>());
                        
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 6. Le decimos al sistema: "Conozco a este usuario, déjalo pasar"
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        
        // 7. Finalmente, enviamos la petición al siguiente paso (tu Controlador)
        chain.doFilter(request, response);
    }
}