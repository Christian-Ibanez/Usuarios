package com.SanosySalvos.Usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data // Anotación de Lombok para generar Getters, Setters y toString automáticamente
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 100)
    private String correoElectronico;

    @Column(nullable = false)
    private String contrasena; // Aquí se guardará el hash generado por Bcrypt

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol = RolUsuario.CIUDADANO; // <-- Asignación por defecto

    @Column(name = "cuenta_validada", nullable = false)
    private Boolean cuentaValidada = false; // Falso por defecto. Útil para aprobar veterinarias

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(name = "url_documento_validacion", length = 500)
    private String urlDocumentoValidacion; // Aquí guardaremos la ruta o enlace al PDF/Imagen

    // Método que se ejecuta automáticamente antes de guardar por primera vez en PostgreSQL
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}