package com.bolivar.polizas.service;

import com.bolivar.polizas.exception.RecursoNoEncontradoException;
import com.bolivar.polizas.exception.ReglaNegocioException;
import com.bolivar.polizas.model.EstadoPoliza;
import com.bolivar.polizas.model.EstadoRiesgo;
import com.bolivar.polizas.model.Poliza;
import com.bolivar.polizas.model.Riesgo;
import com.bolivar.polizas.model.TipoPoliza;
import com.bolivar.polizas.repository.PolizaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio de las pólizas: consulta, renovación y cancelación.
 */
@Service
public class PolizaService {

    private static final BigDecimal CIEN = new BigDecimal("100");
    /** Meses de vigencia asumidos cuando la póliza no tiene fechas definidas. */
    private static final long MESES_POR_DEFECTO = 12L;

    private final PolizaRepository polizaRepository;
    private final CoreMockService coreMockService;

    public PolizaService(PolizaRepository polizaRepository, CoreMockService coreMockService) {
        this.polizaRepository = polizaRepository;
        this.coreMockService = coreMockService;
    }

    /**
     * Lista pólizas filtrando opcionalmente por tipo y/o estado.
     * Si un filtro viene nulo, no se aplica.
     */
    @Transactional(readOnly = true)
    public List<Poliza> listar(TipoPoliza tipo, EstadoPoliza estado) {
        if (tipo != null && estado != null) {
            return polizaRepository.findByTipoAndEstado(tipo, estado);
        }
        if (tipo != null) {
            return polizaRepository.findByTipo(tipo);
        }
        if (estado != null) {
            return polizaRepository.findByEstado(estado);
        }
        return polizaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Poliza obtener(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Póliza no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<Riesgo> listarRiesgos(Long polizaId) {
        return obtener(polizaId).getRiesgos();
    }

    /**
     * Renueva una póliza: ajusta el canon según el IPC, recalcula la prima
     * y cambia el estado a RENOVADA.
     *
     * @param id identificador de la póliza
     * @return la póliza renovada
     * @throws RecursoNoEncontradoException si la póliza no existe
     * @throws ReglaNegocioException        si la póliza está cancelada
     */
    @Transactional
    public Poliza renovar(Long id) {
        Poliza poliza = obtener(id);

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se puede renovar una póliza cancelada");
        }

        if (poliza.getVigenciaInicio() == null || poliza.getVigenciaFin() == null) {
            throw new ReglaNegocioException(
                    "No se puede renovar: la póliza no tiene definido el periodo de vigencia");
        }

        BigDecimal ipc = poliza.getIpc() == null ? BigDecimal.ZERO : poliza.getIpc();
        BigDecimal factor = BigDecimal.ONE.add(ipc.divide(CIEN, 6, RoundingMode.HALF_UP));

        BigDecimal nuevoCanon = poliza.getCanonMensual().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        poliza.setCanonMensual(nuevoCanon);

        long meses = calcularMesesVigencia(poliza);
        BigDecimal nuevaPrima = nuevoCanon.multiply(BigDecimal.valueOf(meses)).setScale(2, RoundingMode.HALF_UP);
        poliza.setPrima(nuevaPrima);

        poliza.setEstado(EstadoPoliza.RENOVADA);

        Poliza guardada = polizaRepository.save(poliza);
        coreMockService.enviarEvento("ACTUALIZACION", guardada.getId());
        return guardada;
    }

    /**
     * Cancela una póliza y, en cascada, todos sus riesgos.
     */
    @Transactional
    public Poliza cancelar(Long id) {
        Poliza poliza = obtener(id);

        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("La póliza ya se encuentra cancelada");
        }

        poliza.setEstado(EstadoPoliza.CANCELADA);
        poliza.getRiesgos().forEach(riesgo -> riesgo.setEstado(EstadoRiesgo.CANCELADO));

        Poliza guardada = polizaRepository.save(poliza);
        coreMockService.enviarEvento("CANCELACION", guardada.getId());
        return guardada;
    }

    /**
     * Calcula los meses del periodo de vigencia. Asume que las fechas ya fueron
     * validadas como no nulas por el llamador (ver renovar).
     */
    private long calcularMesesVigencia(Poliza poliza) {
        long meses = java.time.temporal.ChronoUnit.MONTHS.between(
                poliza.getVigenciaInicio(), poliza.getVigenciaFin());
        return meses <= 0 ? MESES_POR_DEFECTO : meses;
    }
}
