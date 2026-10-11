package com.bolivar.polizas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bolivar.polizas.exception.RecursoNoEncontradoException;
import com.bolivar.polizas.exception.ReglaNegocioException;
import com.bolivar.polizas.model.EstadoPoliza;
import com.bolivar.polizas.model.EstadoRiesgo;
import com.bolivar.polizas.model.Poliza;
import com.bolivar.polizas.model.Riesgo;
import com.bolivar.polizas.model.TipoPoliza;
import com.bolivar.polizas.repository.PolizaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private CoreMockService coreMockService;

    @InjectMocks
    private PolizaService polizaService;

    private Poliza polizaColectivaActiva;

    @BeforeEach
    void setUp() {
        polizaColectivaActiva = new Poliza();
        polizaColectivaActiva.setId(1L);
        polizaColectivaActiva.setTipo(TipoPoliza.COLECTIVA);
        polizaColectivaActiva.setEstado(EstadoPoliza.ACTIVA);
        polizaColectivaActiva.setVigenciaInicio(LocalDate.of(2026, 1, 1));
        polizaColectivaActiva.setVigenciaFin(LocalDate.of(2027, 1, 1));
        polizaColectivaActiva.setCanonMensual(new BigDecimal("2000000.00"));
        polizaColectivaActiva.setPrima(new BigDecimal("24000000.00"));
        polizaColectivaActiva.setIpc(new BigDecimal("12.00"));

        Riesgo r1 = new Riesgo();
        r1.setId(1L);
        r1.setEstado(EstadoRiesgo.ACTIVO);
        r1.setDatosInmueble("Apto 301");
        polizaColectivaActiva.addRiesgo(r1);
    }

    @Test
    void renovar_ajustaCanonYPrimaPorIpcYCambiaEstado() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaColectivaActiva));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> inv.getArgument(0));

        Poliza resultado = polizaService.renovar(1L);

        // canon 2.000.000 * 1.12 = 2.240.000
        assertThat(resultado.getCanonMensual()).isEqualByComparingTo("2240000.00");
        // prima = canon * 12 meses = 26.880.000
        assertThat(resultado.getPrima()).isEqualByComparingTo("26880000.00");
        assertThat(resultado.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
        verify(coreMockService).enviarEvento(anyString(), eq(1L));
    }

    @Test
    void renovar_polizaCancelada_lanzaReglaNegocio() {
        polizaColectivaActiva.setEstado(EstadoPoliza.CANCELADA);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaColectivaActiva));

        assertThatThrownBy(() -> polizaService.renovar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cancelada");

        verify(polizaRepository, never()).save(any());
        verify(coreMockService, never()).enviarEvento(anyString(), any());
    }

    @Test
    void renovar_polizaInexistente_lanzaNoEncontrado() {
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> polizaService.renovar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cancelar_cancelaPolizaYTodosSusRiesgos() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaColectivaActiva));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> inv.getArgument(0));

        Poliza resultado = polizaService.cancelar(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPoliza.CANCELADA);
        assertThat(resultado.getRiesgos())
                .isNotEmpty()
                .allMatch(r -> r.getEstado() == EstadoRiesgo.CANCELADO);
    }

    @Test
    void cancelar_polizaYaCancelada_lanzaReglaNegocio() {
        polizaColectivaActiva.setEstado(EstadoPoliza.CANCELADA);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaColectivaActiva));

        assertThatThrownBy(() -> polizaService.cancelar(1L))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void listar_conTipoYEstado_usaConsultaPorAmbos() {
        when(polizaRepository.findByTipoAndEstado(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA))
                .thenReturn(List.of(polizaColectivaActiva));

        List<Poliza> resultado = polizaService.listar(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA);

        assertThat(resultado).hasSize(1);
        verify(polizaRepository).findByTipoAndEstado(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA);
    }

    @Test
    void listar_sinFiltros_devuelveTodas() {
        when(polizaRepository.findAll()).thenReturn(List.of(polizaColectivaActiva));

        List<Poliza> resultado = polizaService.listar(null, null);

        assertThat(resultado).hasSize(1);
        verify(polizaRepository).findAll();
    }

    @Test
    void listar_soloTipo_usaConsultaPorTipo() {
        when(polizaRepository.findByTipo(TipoPoliza.COLECTIVA))
                .thenReturn(List.of(polizaColectivaActiva));

        List<Poliza> resultado = polizaService.listar(TipoPoliza.COLECTIVA, null);

        assertThat(resultado).hasSize(1);
        verify(polizaRepository).findByTipo(TipoPoliza.COLECTIVA);
    }

    @Test
    void listar_soloEstado_usaConsultaPorEstado() {
        when(polizaRepository.findByEstado(EstadoPoliza.ACTIVA))
                .thenReturn(List.of(polizaColectivaActiva));

        List<Poliza> resultado = polizaService.listar(null, EstadoPoliza.ACTIVA);

        assertThat(resultado).hasSize(1);
        verify(polizaRepository).findByEstado(EstadoPoliza.ACTIVA);
    }

    @Test
    void renovar_conIpcNull_noAjustaCanonPeroRecalculaPrima() {
        polizaColectivaActiva.setIpc(null);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaColectivaActiva));
        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> inv.getArgument(0));

        Poliza resultado = polizaService.renovar(1L);

        // ipc null => canon no cambia (2.000.000); prima = canon * 12 meses (vigencia 1 año)
        assertThat(resultado.getCanonMensual()).isEqualByComparingTo("2000000.00");
        assertThat(resultado.getPrima()).isEqualByComparingTo("24000000.00");
        assertThat(resultado.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
    }

    @Test
    void renovar_sinPeriodoDeVigencia_lanzaReglaNegocio() {
        polizaColectivaActiva.setVigenciaInicio(null);
        polizaColectivaActiva.setVigenciaFin(null);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaColectivaActiva));

        assertThatThrownBy(() -> polizaService.renovar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("vigencia");

        verify(polizaRepository, never()).save(any());
    }

    @Test
    void listarRiesgos_devuelveLosDeLaPoliza() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaColectivaActiva));

        assertThat(polizaService.listarRiesgos(1L)).hasSize(1);
    }
}
