package com.bolivar.polizas.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de integración de la API completa, cargando el contexto de Spring,
 * la base H2 en memoria y los datos de ejemplo (data.sql).
 *
 * Cada prueba corre dentro de una transacción que se revierte al finalizar
 * (@Transactional), de modo que los cambios de un test no afectan a los demás.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PolizaControllerIT {

    private static final String API_KEY_HEADER = "api-key";
    private static final String API_KEY = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sinApiKey_devuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/polizas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiKeyIncorrecta_devuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/polizas").header(API_KEY_HEADER, "incorrecta"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarPolizas_conApiKey_devuelveTodas() throws Exception {
        mockMvc.perform(get("/api/v1/polizas").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void listarPolizas_filtradasPorTipoYEstado() throws Exception {
        mockMvc.perform(get("/api/v1/polizas")
                        .param("tipo", "COLECTIVA")
                        .param("estado", "ACTIVA")
                        .header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo", is("COLECTIVA")));
    }

    @Test
    void listarRiesgosDePoliza() throws Exception {
        mockMvc.perform(get("/api/v1/polizas/1/riesgos").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void renovarPoliza_ajustaCanonYPrima() throws Exception {
        mockMvc.perform(post("/api/v1/polizas/1/renovar").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("RENOVADA")))
                .andExpect(jsonPath("$.canonMensual", is(2240000.00)));
    }

    @Test
    void renovarPolizaCancelada_devuelve409() throws Exception {
        mockMvc.perform(post("/api/v1/polizas/3/renovar").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isConflict());
    }

    @Test
    void agregarRiesgoAIndividual_devuelve409() throws Exception {
        mockMvc.perform(post("/api/v1/polizas/2/riesgos")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datosInmueble\":\"Apto X\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void agregarRiesgoAColectiva_devuelve201() throws Exception {
        mockMvc.perform(post("/api/v1/polizas/1/riesgos")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datosInmueble\":\"Apto 303, Edificio Central\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("ACTIVO")));
    }

    @Test
    void coreMockEvento_devuelve200() throws Exception {
        mockMvc.perform(post("/api/v1/core-mock/evento")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")));
    }

    @Test
    void agregarRiesgoSinDatosInmueble_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/polizas/1/riesgos")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void coreMockEventoSinCampos_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/core-mock/evento")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void renovarPolizaInexistente_devuelve404() throws Exception {
        mockMvc.perform(post("/api/v1/polizas/9999/renovar").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarPoliza_devuelveCanceladaConRiesgosCancelados() throws Exception {
        mockMvc.perform(post("/api/v1/polizas/1/cancelar").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADA")))
                .andExpect(jsonPath("$.riesgos[0].estado", is("CANCELADO")));
    }

    @Test
    void cancelarRiesgo_viaEndpoint_devuelveCancelado() throws Exception {
        mockMvc.perform(post("/api/v1/riesgos/3/cancelar").header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")));
    }
}
