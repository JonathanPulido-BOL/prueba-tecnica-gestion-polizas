package com.bolivar.polizas.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio
 * (por ejemplo, renovar una póliza cancelada o agregar un riesgo a una individual).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
