package com.bolivar.polizas.controller;

import com.bolivar.polizas.model.Riesgo;
import com.bolivar.polizas.service.RiesgoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones directas sobre riesgos.
 */
@RestController
@RequestMapping("/api/v1/riesgos")
@Tag(name = "Riesgos", description = "Operaciones sobre riesgos")
public class RiesgoController {

    private final RiesgoService riesgoService;

    public RiesgoController(RiesgoService riesgoService) {
        this.riesgoService = riesgoService;
    }

    /**
     * Cancela un riesgo por su identificador.
     *
     * @param id identificador del riesgo a cancelar
     * @return el riesgo cancelado
     */
    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar riesgo")
    public ResponseEntity<Riesgo> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(riesgoService.cancelarRiesgo(id));
    }
}
