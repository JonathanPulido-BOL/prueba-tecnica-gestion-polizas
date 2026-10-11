package com.bolivar.polizas.controller;

import com.bolivar.polizas.dto.CrearRiesgoRequest;
import com.bolivar.polizas.model.EstadoPoliza;
import com.bolivar.polizas.model.Poliza;
import com.bolivar.polizas.model.Riesgo;
import com.bolivar.polizas.model.TipoPoliza;
import com.bolivar.polizas.service.PolizaService;
import com.bolivar.polizas.service.RiesgoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de pólizas y sus riesgos.
 */
@RestController
@RequestMapping("/api/v1/polizas")
@Tag(name = "Pólizas", description = "Gestión de pólizas de arrendamiento y sus riesgos")
public class PolizaController {

    private final PolizaService polizaService;
    private final RiesgoService riesgoService;

    public PolizaController(PolizaService polizaService, RiesgoService riesgoService) {
        this.polizaService = polizaService;
        this.riesgoService = riesgoService;
    }

    /**
     * Lista las pólizas, filtrando opcionalmente por tipo y estado.
     *
     * @param tipo   tipo de póliza (INDIVIDUAL o COLECTIVA); opcional
     * @param estado estado de la póliza (ACTIVA, RENOVADA o CANCELADA); opcional
     * @return lista de pólizas que cumplen los filtros indicados
     */
    @GetMapping
    @Operation(summary = "Listar pólizas", description = "Lista pólizas con filtros opcionales por tipo y estado")
    public List<Poliza> listar(@RequestParam(required = false) TipoPoliza tipo,
                               @RequestParam(required = false) EstadoPoliza estado) {
        return polizaService.listar(tipo, estado);
    }

    /**
     * Lista los riesgos asociados a una póliza.
     *
     * @param id identificador de la póliza
     * @return lista de riesgos de la póliza
     */
    @GetMapping("/{id}/riesgos")
    @Operation(summary = "Listar riesgos de una póliza")
    public List<Riesgo> listarRiesgos(@PathVariable Long id) {
        return polizaService.listarRiesgos(id);
    }

    /**
     * Renueva una póliza: ajusta el canon y la prima según el IPC y cambia su estado a RENOVADA.
     *
     * @param id identificador de la póliza a renovar
     * @return la póliza renovada
     */
    @PostMapping("/{id}/renovar")
    @Operation(summary = "Renovar póliza", description = "Ajusta canon y prima por IPC; el estado pasa a RENOVADA")
    public ResponseEntity<Poliza> renovar(@PathVariable Long id) {
        return ResponseEntity.ok(polizaService.renovar(id));
    }

    /**
     * Cancela una póliza y, en cascada, todos sus riesgos.
     *
     * @param id identificador de la póliza a cancelar
     * @return la póliza cancelada
     */
    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar póliza", description = "Cancela la póliza y todos sus riesgos")
    public ResponseEntity<Poliza> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(polizaService.cancelar(id));
    }

    /**
     * Agrega un riesgo a una póliza. Solo se permite en pólizas de tipo COLECTIVA.
     *
     * @param id      identificador de la póliza
     * @param request datos del riesgo a agregar
     * @return el riesgo creado
     */
    @PostMapping("/{id}/riesgos")
    @Operation(summary = "Agregar riesgo", description = "Agrega un riesgo a una póliza colectiva")
    public ResponseEntity<Riesgo> agregarRiesgo(@PathVariable Long id,
                                                @Valid @RequestBody CrearRiesgoRequest request) {
        Riesgo creado = riesgoService.agregarRiesgo(id, request.getDatosInmueble());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
