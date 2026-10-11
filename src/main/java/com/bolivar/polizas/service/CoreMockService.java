package com.bolivar.polizas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Simula el servicio agnóstico de edición hacia el CORE legado.
 * Su único propósito es registrar en logs que la operación se intentó enviar al CORE.
 */
@Slf4j
@Service
public class CoreMockService {

    /**
     * Registra en logs el intento de envío de un evento al CORE.
     *
     * @param evento   tipo de evento (por ejemplo, ACTUALIZACION, CANCELACION)
     * @param polizaId identificador de la póliza afectada
     */
    public void enviarEvento(String evento, Long polizaId) {
        log.info("[CORE-MOCK] Intento de envío al CORE -> evento='{}', polizaId={}", evento, polizaId);
    }
}
