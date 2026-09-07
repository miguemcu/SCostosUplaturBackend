package com.miguel_mejia.fincostos_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pago_nomina")
@Getter
@Setter
@NoArgsConstructor
public class PagoNomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finca_id", nullable = false)
    private Finca finca;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gasto_asociado_id")
    private Gasto gastoAsociado;

    @Column(nullable = false, length = 50)
    private String mes;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago = LocalDate.now();

    @Column(name = "suma_salarios", nullable = false, precision = 12, scale = 2)
    private BigDecimal sumaSalarios;

    @Column(precision = 12, scale = 2)
    private BigDecimal deducciones = BigDecimal.ZERO;

    @Column(name = "total_pagado", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal totalPagado;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "client_uuid", nullable = false, unique = true)
    private UUID clientUuid;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (clientUuid == null) {
            clientUuid = UUID.randomUUID();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
