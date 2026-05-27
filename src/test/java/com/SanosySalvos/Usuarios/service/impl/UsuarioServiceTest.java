package com.SanosySalvos.Usuarios.service.impl;

import com.SanosySalvos.Usuarios.model.RolUsuario;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    // ELIMINADO: @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setNombreCompleto("Juan Perez");
        usuarioPrueba.setCorreoElectronico("juan@email.com");
        usuarioPrueba.setRol(RolUsuario.CIUDADANO);
    }

    @Test
    void testRegistrarUsuario_Exito() {
        
        usuarioPrueba.setContrasena("superSecreta123"); // Ahora se guarda tal cual

        when(usuarioRepository.existsByCorreoElectronico(usuarioPrueba.getCorreoElectronico())).thenReturn(false);
        // ELIMINADO: when(passwordEncoder.encode(anyString())).thenReturn("hashFalso123");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioPrueba);

        Usuario resultado = usuarioService.registrarUsuario(usuarioPrueba);

        assertNotNull(resultado);
        assertEquals("juan@email.com", resultado.getCorreoElectronico());
        assertTrue(resultado.getCuentaValidada()); 
        verify(usuarioRepository, times(1)).save(any(Usuario.class)); 
    }

    @Test
    void testRegistrarUsuario_FallaPorCorreoDuplicado() {
        
        when(usuarioRepository.existsByCorreoElectronico(usuarioPrueba.getCorreoElectronico())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.registrarUsuario(usuarioPrueba);
        });
        
        assertEquals("Error: El correo electrónico ya está registrado.", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class)); 
    }

    @Test
    void testObtenerUsuarioPorCorreo_Exito() {
    
        when(usuarioRepository.findByCorreoElectronico("juan@email.com"))
                .thenReturn(Optional.of(usuarioPrueba));

        Usuario resultado = usuarioService.obtenerUsuarioPorCorreo("juan@email.com");

        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.getNombreCompleto());
    }

    @Test
    void testObtenerInstitucionesPendientes() {
        Usuario clinica = new Usuario();
        clinica.setRol(RolUsuario.VETERINARIA);
        clinica.setCuentaValidada(false);
        
        List<RolUsuario> rolesInstitucionales = List.of(
                RolUsuario.VETERINARIA,
                RolUsuario.REFUGIO,
                RolUsuario.MUNICIPALIDAD
        );
        
        when(usuarioRepository.findByRolInAndCuentaValidadaFalse(rolesInstitucionales))
                .thenReturn(Arrays.asList(clinica));

        List<Usuario> resultados = usuarioService.obtenerInstitucionesPendientes();

        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
        assertEquals(RolUsuario.VETERINARIA, resultados.get(0).getRol());
    }

    @Test
    void testAprobarCuentaInstitucional_Exito() {
        Usuario clinicaPendiente = new Usuario();
        clinicaPendiente.setId(2L);
        clinicaPendiente.setCuentaValidada(false);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(clinicaPendiente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(clinicaPendiente); 

        Usuario resultado = usuarioService.aprobarCuentaInstitucional(2L);

        assertTrue(resultado.getCuentaValidada()); 
        verify(usuarioRepository, times(1)).save(clinicaPendiente);
    }

    @Test
    void testSolicitarCambioRol_Exito() {

        Usuario ciudadano = new Usuario();
        ciudadano.setId(1L);
        ciudadano.setRol(RolUsuario.CIUDADANO);
        ciudadano.setCuentaValidada(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ciudadano));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(ciudadano);

        Usuario resultado = usuarioService.solicitarCambioRol(1L, RolUsuario.VETERINARIA, "ruta_documento.pdf");

        assertEquals(RolUsuario.VETERINARIA, resultado.getRol());
        assertFalse(resultado.getCuentaValidada()); 
        assertEquals("ruta_documento.pdf", resultado.getUrlDocumentoValidacion());
        verify(usuarioRepository, times(1)).save(ciudadano);
    }

    @Test
    void testSolicitarCambioRol_FallaPorRolInvalido() {

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.ADMINISTRADOR, "ruta_documento.pdf");
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testSolicitarCambioRol_FallaPorDocumentoVacio() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.VETERINARIA, ""); 
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void obtenerUsuarioPorCorreo_LanzaExcepcion_SiNoExiste() {
        when(usuarioRepository.findByCorreoElectronico("noexiste@mail.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            usuarioService.obtenerUsuarioPorCorreo("noexiste@mail.com");
        });
    }

    @Test
    void solicitarCambioRol_LanzaExcepcion_SiUsuarioNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(999L, RolUsuario.REFUGIO, "Refugio Esperanza");
        });
    }

    @Test
    void solicitarCambioRol_LanzaExcepcion_SiRolNoEsInstitucional() {
        assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.CIUDADANO, "Documento.pdf");
        });
    }

    @Test
    void solicitarCambioRol_LanzaExcepcion_SiDocumentoEstaVacio() {
        assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.REFUGIO, "   ");
        });
    }

    @Test
    void aprobarCuentaInstitucional_LanzaExcepcion_SiUsuarioNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            usuarioService.aprobarCuentaInstitucional(999L);
        });
    }

    @Test
    void solicitarCambioRol_LanzaExcepcion_SiDocumentoEsNulo() {
        assertThrows(ResponseStatusException.class, () -> {
            usuarioService.solicitarCambioRol(1L, RolUsuario.REFUGIO, null);
        });
    }

    //TEST CREAR PERFIL VACÍO

    @Test
    void crearPerfilVacio_Exito_GuardaUsuarioConRolCiudadano() {
        // Arrange
        String correo = "nuevo@test.com";
        when(usuarioRepository.existsByCorreoElectronico(correo)).thenReturn(false);
        
        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setCorreoElectronico(correo);
        usuarioGuardado.setRol(RolUsuario.CIUDADANO);
        usuarioGuardado.setCuentaValidada(true);
        
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act
        Usuario resultado = usuarioService.crearPerfilVacio(correo);

        // Assert
        assertNotNull(resultado);
        assertEquals(correo, resultado.getCorreoElectronico());
        assertEquals(RolUsuario.CIUDADANO, resultado.getRol());
        assertTrue(resultado.getCuentaValidada()); // o la aserción que corresponda a tu boolean
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void crearPerfilVacio_LanzaExcepcion_SiCorreoYaExiste() {
        // Arrange
        String correo = "duplicado@test.com";
        when(usuarioRepository.existsByCorreoElectronico(correo)).thenReturn(true);

        // Act & Assert
        org.springframework.web.server.ResponseStatusException exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class, 
                () -> usuarioService.crearPerfilVacio(correo)
        );

        assertEquals(org.springframework.http.HttpStatus.CONFLICT, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    //TESTS CIRCUIT BREAKER 

    @Test
    void notificarNuevoUsuario_LanzaRuntimeException() {
        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class, 
                () -> usuarioService.notificarNuevoUsuario("test@test.com")
        );
        assertEquals("¡El microservicio de Notificaciones está caído!", exception.getMessage());
    }

    @Test
    void notificacionFallback_RetornaMensajeDeContingencia() {
        // Arrange
        String correo = "test@test.com";
        Exception excepcionSimulada = new RuntimeException("Timeout");

        // Act
        String resultado = usuarioService.notificacionFallback(correo, excepcionSimulada);

        // Assert
        assertEquals("Usuario registrado, pero el correo de bienvenida se enviará más tarde.", resultado);
    }

}