package com.bolivar.polizas.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad Poliza. Representa una póliza de arrendamiento (individual o colectiva).
 * Una póliza tiene uno o muchos riesgos (individual: exactamente 1; colectiva: 1..N).
 */
@Entity
@Table(name = "poliza")
@Getter
@Setter
@NoArgsConstructor
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPoliza estado;

    private String tomador;

    private String asegurado;

    private String beneficiario;

    @Column(name = "vigencia_inicio")
    private LocalDate vigenciaInicio;

    @Column(name = "vigencia_fin")
    private LocalDate vigenciaFin;

    /** Valor del canon mensual de arrendamiento. */
    @Column(name = "canon_mensual", precision = 15, scale = 2)
    private BigDecimal canonMensual;

    /** Prima = canon mensual x número de meses de vigencia. */
    @Column(precision = 15, scale = 2)
    private BigDecimal prima;

    /** Porcentaje de IPC aplicado en la renovación (ej. 12.5 = 12.5%). */
    @Column(precision = 5, scale = 2)
    private BigDecimal ipc;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Riesgo> riesgos = new ArrayList<>();

    /** Agrega un riesgo manteniendo la relación bidireccional consistente. */
    public void addRiesgo(Riesgo riesgo) {
        riesgo.setPoliza(this);
        this.riesgos.add(riesgo);
    }
}
