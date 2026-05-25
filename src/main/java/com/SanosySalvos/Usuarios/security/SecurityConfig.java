package com.SanosySalvos.Usuarios.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Enciende las defensas de Spring Boot
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Apagamos el CSRF porque estamos usando Tokens (JWT) en vez de Cookies de sesión
            .csrf(csrf -> csrf.disable())
            
            // 2. Configuramos nuestras reglas de acceso (Rutas públicas vs privadas)
            .authorizeHttpRequests(auth -> auth
                // Dejamos que cualquier persona pueda registrarse o iniciar sesión
                .requestMatchers(HttpMethod.POST, "/api/usuarios/registro").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/usuarios/login").permitAll()
                
                // (Opcional) Si quieres que Swagger/Postman vea la documentación sin token
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                
                // Cualquier otra petición a este microservicio requiere estar autenticado
                .anyRequest().authenticated()
            )
            
            // 3. Le decimos a Spring que NO guarde sesiones en la memoria del servidor
            // Esto es vital para los microservicios: cada petición debe traer su propio token
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 4. Colocamos nuestro Filtro (La Aduana) justo antes del filtro predeterminado de Spring
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Herramienta para encriptar las contraseñas en la base de datos (Bcrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Herramienta oficial de Spring para gestionar los inicios de sesión
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}