package com.SanosySalvos.Usuarios.service.impl;

import com.SanosySalvos.Usuarios.model.RolUsuario; // Asegúrate de tener este Enum
import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito para esta clase de pruebas
public class UsuarioServiceTest {

    // 1. @Mock: Simulamos la base de datos (El "Repository"). No tocará PostgreSQL real.
    @Mock
    private UsuarioRepository usuarioRepository;

    // 2. @InjectMocks: Inyecta el Mock anterior dentro de nuestro servicio real.
    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // Objeto de prueba que reutilizaremos
    private Usuario usuarioPrueba;

    // 3. @BeforeEach: Este método se ejecuta automáticamente antes de cada @Test
    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setNombreCompleto("Juan Perez");
        usuarioPrueba.setCorreoElectronico("juan@email.com");
        usuarioPrueba.setRol(RolUsuario.CIUDADANO);
    }

    // --- PRUEBA 1: registrarUsuario ---
    @Test
    void testRegistrarUsuario_Exito() {
        // Arrange (Preparar): Le decimos a Mockito qué responder cuando el servicio llame al Repositorio
        when(usuarioRepository.existsByCorreoElectronico(usuarioPrueba.getCorreoElectronico())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioPrueba);

        // Act (Actuar): Ejecutamos el método real de nuestro servicio
        Usuario resultado = usuarioService.registrarUsuario(usuarioPrueba);

        // Assert (Afirmar): Verificamos que el resultado sea el esperado
        assertNotNull(resultado);
        assertEquals("juan@email.com", resultado.getCorreoElectronico());
        assertTrue(resultado.isCuentaValidada()); // Porque es CIUDADANO, debería auto-validarse
        verify(usuarioRepository, times(1)).save(any(Usuario.class)); // Verifica que save() se llamó 1 vez
    }

    @Test
    void testRegistrarUsuario_FallaPorCorreoDuplicado() {
        // Arrange: Simulamos que el correo ya existe en la base de datos
        when(usuarioRepository.existsByCorreoElectronico(usuarioPrueba.getCorreoElectronico())).thenReturn(true);

        // Act & Assert: Verificamos que lance la excepción correcta
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.registrarUsuario(usuarioPrueba);
        });
        
        assertEquals("Error: El correo electrónico ya está registrado.", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class)); // Aseguramos que nunca intentó guardar
    }

    // --- PRUEBA 2: obtenerUsuarioPorCorreo[cite: 3] ---
    @Test
    void testObtenerUsuarioPorCorreo_Exito() {
        // Arrange
        when(usuarioRepository.findByCorreoElectronico("juan@email.com"))
                .thenReturn(Optional.of(usuarioPrueba));

        // Act
        Usuario resultado = usuarioService.obtenerUsuarioPorCorreo("juan@email.com");

        // Assert
        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.getNombreCompleto());
    }

    // --- PRUEBA 3: obtenerInstitucionesPendientes[cite: 3] ---
    @Test
    void testObtenerInstitucionesPendientes() {
        // Arrange
        Usuario clinica = new Usuario();
        clinica.setRol(RolUsuario.VETERINARIA);
        clinica.setCuentaValidada(false);
        
        when(usuarioRepository.findByRolAndCuentaValidadaFalse(RolUsuario.VETERINARIA))
                .thenReturn(Arrays.asList(clinica));

        // Act
        List<Usuario> resultados = usuarioService.obtenerInstitucionesPendientes();

        // Assert
        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
        assertEquals(RolUsuario.VETERINARIA, resultados.get(0).getRol());
    }

    // --- PRUEBA 4: aprobarCuentaInstitucional[cite: 3] ---
    @Test
    void testAprobarCuentaInstitucional_Exito() {
        // Arrange
        Usuario clinicaPendiente = new Usuario();
        clinicaPendiente.setId(2L);
        clinicaPendiente.setCuentaValidada(false);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(clinicaPendiente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(clinicaPendiente); // Retorna la misma instancia

        // Act
        Usuario resultado = usuarioService.aprobarCuentaInstitucional(2L);

        // Assert
        assertTrue(resultado.isCuentaValidada()); // Verificamos que el estado cambió a true
        verify(usuarioRepository, times(1)).save(clinicaPendiente);
    }
}