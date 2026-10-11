package com.bolivar.polizas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.bolivar.polizas.repository.RiesgoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RiesgoServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private RiesgoRepository riesgoRepository;

    @Mock
    private CoreMockService coreMockService;

    @InjectMocks
    private RiesgoService riesgoService;

    private Poliza polizaColectiva(EstadoPoliza estado) {
        Poliza p = new Poliza();
        p.setId(1L);
        p.setTipo(TipoPoliza.COLECTIVA);
        p.setEstado(estado);
        return p;
    }

    private Poliza polizaIndividual() {
        Poliza p = new Poliza();
        p.setId(2L);
        p.setTipo(TipoPoliza.INDIVIDUAL);
        p.setEstado(EstadoPoliza.ACTIVA);
        return p;
    }

    @Test
    void agregarRiesgo_aColectivaActiva_creaRiesgoActivo() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaColectiva(EstadoPoliza.ACTIVA)));
        when(riesgoRepository.save(any(Riesgo.class))).thenAnswer(inv -> {
            Riesgo r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        Riesgo creado = riesgoService.agregarRiesgo(1L, "Apto 303");

        assertThat(creado.getId()).isEqualTo(100L);
        assertThat(creado.getEstado()).isEqualTo(EstadoRiesgo.ACTIVO);
        assertThat(creado.getDatosInmueble()).isEqualTo("Apto 303");
        verify(coreMockService).enviarEvento(anyString(), any());
    }

    @Test
    void agregarRiesgo_aIndividual_lanzaReglaNegocio() {
        when(polizaRepository.findById(2L)).thenReturn(Optional.of(polizaIndividual()));

        assertThatThrownBy(() -> riesgoService.agregarRiesgo(2L, "Apto X"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("colectivas");

        verify(riesgoRepository, never()).save(any());
    }

    @Test
    void agregarRiesgo_aPolizaCancelada_lanzaReglaNegocio() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaColectiva(EstadoPoliza.CANCELADA)));

        assertThatThrownBy(() -> riesgoService.agregarRiesgo(1L, "Apto X"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void agregarRiesgo_aPolizaInexistente_lanzaNoEncontrado() {
        when(polizaRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riesgoService.agregarRiesgo(77L, "Apto X"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cancelarRiesgo_activo_pasaACancelado() {
        Poliza poliza = polizaColectiva(EstadoPoliza.ACTIVA);
        Riesgo riesgo = new Riesgo();
        riesgo.setId(5L);
        riesgo.setEstado(EstadoRiesgo.ACTIVO);
        riesgo.setPoliza(poliza);

        when(riesgoRepository.findById(5L)).thenReturn(Optional.of(riesgo));
        when(riesgoRepository.save(any(Riesgo.class))).thenAnswer(inv -> inv.getArgument(0));

        Riesgo resultado = riesgoService.cancelarRiesgo(5L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoRiesgo.CANCELADO);
        verify(coreMockService).enviarEvento(anyString(), any());
    }

    @Test
    void cancelarRiesgo_yaCancelado_lanzaReglaNegocio() {
        Riesgo riesgo = new Riesgo();
        riesgo.setId(5L);
        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        riesgo.setPoliza(polizaColectiva(EstadoPoliza.ACTIVA));

        when(riesgoRepository.findById(5L)).thenReturn(Optional.of(riesgo));

        assertThatThrownBy(() -> riesgoService.cancelarRiesgo(5L))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
