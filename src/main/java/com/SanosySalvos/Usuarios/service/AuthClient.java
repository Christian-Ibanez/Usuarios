package com.SanosySalvos.Usuarios.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthClient {

    // Le envías el token a tu compañero y él te devuelve un true/false (o los datos del usuario)
    @GetMapping("/api/auth/validar-token")
    boolean validarToken(@RequestHeader("Authorization") String token);
}