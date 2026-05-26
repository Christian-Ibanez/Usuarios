package com.SanosySalvos.Usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.SanosySalvos.Usuarios.model.RolUsuario;
import com.SanosySalvos.Usuarios.model.Usuario;
import com.SanosySalvos.Usuarios.service.UsuarioService;

import com.fasterxml.jackson.databind.ObjectMapper;

// Le decimos a Spring que SOLO levante el entorno web para este controlador
@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    // Herramienta para simular peticiones HTTP (GET, POST, PUT, etc.)
    @Autowired
    private MockMvc mockMvc;

    // Herramienta para convertir objetos Java a JSON y viceversa
    private ObjectMapper objectMapper = new ObjectMapper();

    // Simulamos el servicio (ya probamos el real en la otra clase)
    @MockitoBean
    private UsuarioService usuarioService;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setNombreCompleto("Juan Perez");
        usuarioPrueba.setCorreoElectronico("juan@mail.com");
        usuarioPrueba.setRol(RolUsuario.CIUDADANO);
        usuarioPrueba.setCuentaValidada(true);
    }

    @Test
    void registrarUsuario_DebeRetornar201() throws Exception {
        // Le decimos al servicio falso qué responder
        when(usuarioService.registrarUsuario(any(Usuario.class))).thenReturn(usuarioPrueba);

        // Simulamos un POST a /api/usuarios/registro
        mockMvc.perform(post("/api/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioPrueba))) // Convertimos el usuario a JSON
                
                // Esperamos que HTTP Status sea 201 (Created)
                .andExpect(status().isCreated())
                // Verificamos que el JSON de respuesta tenga el correo correcto
                .andExpect(jsonPath("$.correoElectronico").value("juan@mail.com"));
    }

    @Test
    void obtenerUsuarioPorCorreo_DebeRetornar200() throws Exception {
        when(usuarioService.obtenerUsuarioPorCorreo("juan@mail.com")).thenReturn(usuarioPrueba);

        // Simulamos un GET
        mockMvc.perform(get("/api/usuarios/correo/juan@mail.com")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.nombreCompleto").value("Juan Perez"));
    }

    @Test
    void obtenerInstitucionesPendientes_DebeRetornarListaY200() throws Exception {
        Usuario institucion = new Usuario();
        institucion.setId(2L);
        institucion.setRol(RolUsuario.VETERINARIA);
        List<Usuario> pendientes = Arrays.asList(institucion);

        when(usuarioService.obtenerInstitucionesPendientes()).thenReturn(pendientes);

        mockMvc.perform(get("/api/usuarios/instituciones/pendientes")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                // $.size() verifica el tamaño del arreglo JSON que devuelve
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].rol").value("VETERINARIA"));
    }

    @Test
    void aprobarCuentaInstitucional_DebeRetornar200() throws Exception {
        usuarioPrueba.setRol(RolUsuario.VETERINARIA);
        usuarioPrueba.setCuentaValidada(true);

        when(usuarioService.aprobarCuentaInstitucional(1L)).thenReturn(usuarioPrueba);

        // Simulamos un PUT
        mockMvc.perform(put("/api/usuarios/instituciones/1/aprobar")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaValidada").value(true));
    }

    @Test
    void solicitarCambioRol_DebeRetornar200() throws Exception {
        usuarioPrueba.setRol(RolUsuario.VETERINARIA);
        usuarioPrueba.setCuentaValidada(false);
        usuarioPrueba.setUrlDocumentoValidacion("doc.pdf");

        when(usuarioService.solicitarCambioRol(eq(1L), any(RolUsuario.class), anyString())).thenReturn(usuarioPrueba);

        mockMvc.perform(put("/api/usuarios/1/solicitar-rol")
                .param("nuevoRol", "VETERINARIA")
                .param("urlDocumento", "doc.pdf")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("VETERINARIA"))
                .andExpect(jsonPath("$.cuentaValidada").value(false));
    }

    @Test
    void probarFallback_DebeRetornar200() throws Exception {
        // Simulamos la respuesta que daría tu servicio cuando entra al fallback
        String respuestaFallback = "Usuario registrado, pero el correo de bienvenida se enviará más tarde.";
        when(usuarioService.notificarNuevoUsuario(anyString())).thenReturn(respuestaFallback);

        // Simulamos un GET al endpoint de prueba
        mockMvc.perform(get("/api/usuarios/prueba-fallback")
                .contentType(MediaType.APPLICATION_JSON))
                
                // Esperamos un 200 OK
                .andExpect(status().isOk());
    }
    
}