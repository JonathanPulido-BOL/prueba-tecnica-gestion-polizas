package com.bolivar.polizas.service;

import com.bolivar.polizas.exception.RecursoNoEncontradoException;
import com.bolivar.polizas.exception.ReglaNegocioException;
import com.bolivar.polizas.model.EstadoPoliza;
import com.bolivar.polizas.model.EstadoRiesgo;
import com.bolivar.polizas.model.Poliza;
import com.bolivar.polizas.model.Riesgo;
import com.bolivar.polizas.model.TipoPoliza;
import com.bolivar.polizas.repository.PolizaRepository;
import com.bolivar.polizas.repository.RiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio de los riesgos: agregar (solo colectivas) y cancelar.
 */
@Service
public class RiesgoService {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final CoreMockService coreMockService;

    public RiesgoService(PolizaRepository polizaRepository,
                         RiesgoRepository riesgoRepository,
                         CoreMockService coreMockService) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.coreMockService = coreMockService;
    }

    /**
     * Agrega un riesgo a una póliza. Reglas:
     * - Solo las pólizas COLECTIVAS pueden agregar riesgos.
     * - Una póliza INDIVIDUAL solo puede tener un riesgo.
     * - No se agregan riesgos a pólizas canceladas.
     */
    @Transactional
    public Riesgo agregarRiesgo(Long polizaId, String datosInmueble) {
        Poliza poliza = polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Póliza no encontrada: " + polizaId));

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se pueden agregar riesgos a una póliza cancelada");
        }

        if (poliza.getTipo() == TipoPoliza.INDIVIDUAL) {
            throw new ReglaNegocioException(
                    "Solo las pólizas colectivas pueden agregar riesgos; una póliza individual solo admite un riesgo");
        }

        Riesgo riesgo = new Riesgo();
        riesgo.setEstado(EstadoRiesgo.ACTIVO);
        riesgo.setDatosInmueble(datosInmueble);
        riesgo.setPoliza(poliza);

        Riesgo guardado = riesgoRepository.save(riesgo);
        coreMockService.enviarEvento("ACTUALIZACION", poliza.getId());
        return guardado;
    }

    /**
     * Cancela un riesgo específico por su identificador.
     */
    @Transactional
    public Riesgo cancelarRiesgo(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Riesgo no encontrado: " + riesgoId));

        if (riesgo.getEstado() == EstadoRiesgo.CANCELADO) {
            throw new ReglaNegocioException("El riesgo ya se encuentra cancelado");
        }

        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        Riesgo guardado = riesgoRepository.save(riesgo);
        coreMockService.enviarEvento("CANCELACION", riesgo.getPoliza().getId());
        return guardado;
    }
}
