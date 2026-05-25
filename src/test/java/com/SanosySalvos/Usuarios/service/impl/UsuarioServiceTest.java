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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
        assertTrue(resultado.getCuentaValidada()); // Porque es CIUDADANO, debería auto-validarse
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
        
        // Creamos la lista que espera recibir el repositorio
        List<RolUsuario> rolesInstitucionales = List.of(
                RolUsuario.VETERINARIA,
                RolUsuario.REFUGIO,
                RolUsuario.MUNICIPALIDAD
        );
        
        // Simulamos el nuevo método del repositorio que usa "In"
        when(usuarioRepository.findByRolInAndCuentaValidadaFalse(rolesInstitucionales))
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
        assertTrue(resultado.getCuentaValidada()); // Verificamos que el estado cambió a true
        verify(usuarioRepository, times(1)).save(clinicaPendiente);
    }

    // --- PRUEBA: solicitarCambioRol (El camino feliz) ---
    @Test
    void testSolicitarCambioRol_Exito() {
        // Arrange: Simulamos un ciudadano que ya existe en la base de datos
        Usuario ciudadano = new Usuario();
        ciudadano.setId(1L);
        ciudadano.setRol(RolUsuario.CIUDADANO);
        ciudadano.setCuentaValidada(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ciudadano));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(ciudadano);

        // Act: El ciudadano solicita ser veterinaria y adjunta su documento
        Usuario resultado = usuarioService.solicitarCambioRol(1L, RolUsuario.VETERINARIA, "ruta_documento.pdf");

        // Assert: Verificamos que el rol cambió, se bloqueó la cuenta y se guardó la URL
        assertEquals(RolUsuario.VETERINARIA, resultado.getRol());
        assertFalse(resultado.getCuentaValidada()); // Debe pasar a false
        assertEquals("ruta_documento.pdf", resultado.getUrlDocumentoValidacion());
        verify(usuarioRepository, times(1)).save(ciudadano);
    }

    // --- PRUEBA: solicitarCambioRol (Seguridad: Intento de ser Administrador) ---
    @Test
    void testSolicitarCambioRol_FallaPorRolInvalido() {
        // Act & Assert: Intentamos pedir el rol de ADMINISTRADOR y esperamos que explote con un 403
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.ADMINISTRADOR, "ruta_documento.pdf");
        });

        // Verificamos que el código de error sea exactamente FORBIDDEN (403)
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        
        // Verificamos que NUNCA se haya intentado guardar en la base de datos
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- PRUEBA: solicitarCambioRol (Seguridad: Sin adjuntar documento) ---
    @Test
    void testSolicitarCambioRol_FallaPorDocumentoVacio() {
        // Act & Assert: Pedimos un rol válido, pero enviamos la ruta vacía
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.VETERINARIA, ""); // String vacío
        });

        // Verificamos que el código de error sea exactamente BAD REQUEST (400)
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        
        // Verificamos que NUNCA se haya intentado guardar en la base de datos
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- CAMINOS TRISTES PARA LLEGAR AL 100% ---

    @Test
    void obtenerUsuarioPorCorreo_LanzaExcepcion_SiNoExiste() {
        when(usuarioRepository.findByCorreoElectronico("noexiste@mail.com")).thenReturn(Optional.empty());

        // Cambiamos a RuntimeException.class
        assertThrows(RuntimeException.class, () -> {
            usuarioService.obtenerUsuarioPorCorreo("noexiste@mail.com");
        });
    }

    @Test
    void solicitarCambioRol_LanzaExcepcion_SiUsuarioNoExiste() {
        // Arrange: Simulamos que buscamos un ID falso
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert (Esto cubre el otro lambda$)
        assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(999L, RolUsuario.REFUGIO, "Refugio Esperanza");
        });
    }

    @Test
    void solicitarCambioRol_LanzaExcepcion_SiRolNoEsInstitucional() {
        // Act & Assert: Intentamos pedir el rol CIUDADANO (que no está en la lista de permitidos)
        // Como el "if" está al principio del método, ni siquiera necesitamos simular el repositorio
        assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.CIUDADANO, "Documento.pdf");
        });
    }

    @Test
    void solicitarCambioRol_LanzaExcepcion_SiDocumentoEstaVacio() {
        // Act & Assert: Intentamos pedir un rol válido, pero enviamos un string vacío en el documento
        assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.REFUGIO, "   ");
        });
    }

    @Test
    void aprobarCuentaInstitucional_LanzaExcepcion_SiUsuarioNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Cambiamos a RuntimeException.class
        assertThrows(RuntimeException.class, () -> {
            usuarioService.aprobarCuentaInstitucional(999L);
        });
    }

    @Test
    void solicitarCambioRol_LanzaExcepcion_SiDocumentoEsNulo() {
        // Act & Assert: Intentamos pedir un rol válido, pero enviamos un null literal en el documento
        // Esto cubrirá la primera mitad del operador "||" que le faltaba a JaCoCo
        assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.REFUGIO, null);
        });
    }

}