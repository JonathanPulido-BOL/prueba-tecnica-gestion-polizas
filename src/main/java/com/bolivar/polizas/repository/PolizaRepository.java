package com.bolivar.polizas.repository;

import com.bolivar.polizas.model.EstadoPoliza;
import com.bolivar.polizas.model.Poliza;
import com.bolivar.polizas.model.TipoPoliza;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    List<Poliza> findByTipoAndEstado(TipoPoliza tipo, EstadoPoliza estado);

    List<Poliza> findByTipo(TipoPoliza tipo);

    List<Poliza> findByEstado(EstadoPoliza estado);
}
