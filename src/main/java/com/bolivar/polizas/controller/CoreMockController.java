package com.bolivar.polizas.controller;

import com.bolivar.polizas.dto.EventoCoreRequest;
import com.bolivar.polizas.service.CoreMockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mock del CORE. Recibe un evento y únicamente lo registra en logs,
 * simulando el intento de envío al sistema CORE legado.
 */
@RestController
@RequestMapping("/api/v1/core-mock")
@Tag(name = "CORE Mock", description = "Simulación del envío de eventos al CORE legado")
public class CoreMockController {

    private final CoreMockService coreMockService;

    public CoreMockController(CoreMockService coreMockService) {
        this.coreMockService = coreMockService;
    }

    /**
     * Recibe un evento y lo registra en logs, simulando el envío al CORE.
     *
     * @param request evento y póliza afectada
     * @return confirmación de recepción del evento
     */
    @PostMapping("/evento")
    @Operation(summary = "Registrar evento del CORE",
            description = "Registra en logs el intento de envío de un evento al CORE legado")
    public ResponseEntity<Map<String, Object>> recibirEvento(@Valid @RequestBody EventoCoreRequest request) {
        coreMockService.enviarEvento(request.getEvento(), request.getPolizaId());
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Evento recibido y registrado en logs (simulación de envío al CORE)",
                "evento", request.getEvento(),
                "polizaId", request.getPolizaId()));
    }
}
