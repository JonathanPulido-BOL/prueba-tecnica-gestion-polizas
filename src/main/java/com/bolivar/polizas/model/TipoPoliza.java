package com.bolivar.polizas.model;

/**
 * Tipo de póliza de arrendamiento.
 * INDIVIDUAL: el tomador y asegurado es el arrendatario; admite un único riesgo.
 * COLECTIVA: orientada a inmobiliarias/administraciones; admite uno o muchos riesgos.
 */
public enum TipoPoliza {
    INDIVIDUAL,
    COLECTIVA
}
