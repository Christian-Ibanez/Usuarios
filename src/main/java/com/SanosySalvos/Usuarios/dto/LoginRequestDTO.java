package com.SanosySalvos.Usuarios.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String correoElectronico;
    private String contrasena;
}