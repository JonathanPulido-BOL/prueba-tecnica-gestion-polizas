package com.bolivar.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Cuerpo del evento que se "enviaría" al CORE a través del mock.
 * Ejemplo: { "evento": "ACTUALIZACION", "polizaId": 555 }
 */
@Data
public class EventoCoreRequest {

    @NotBlank(message = "evento es obligatorio")
    private String evento;

    @NotNull(message = "polizaId es obligatorio")
    private Long polizaId;
}
