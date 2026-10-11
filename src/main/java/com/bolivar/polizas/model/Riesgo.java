package com.bolivar.polizas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad Riesgo. Representa un riesgo (inmueble asegurado) asociado a una póliza.
 */
@Entity
@Table(name = "riesgo")
@Getter
@Setter
@NoArgsConstructor
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @JsonIgnore evita la recursión infinita al serializar (Poliza -> riesgos -> poliza -> ...).
    @ManyToOne
    @JoinColumn(name = "poliza_id", nullable = false)
    @JsonIgnore
    private Poliza poliza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRiesgo estado;

    /** Datos del inmueble/riesgo asegurado (por ejemplo, dirección). */
    @Column(name = "datos_inmueble")
    private String datosInmueble;
}
