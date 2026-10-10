package com.bolivar.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Datos para agregar un riesgo a una póliza colectiva.
 */
@Data
public class CrearRiesgoRequest {

    @NotBlank(message = "datosInmueble es obligatorio")
    private String datosInmueble;
}
