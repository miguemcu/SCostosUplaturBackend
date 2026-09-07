package com.miguel_mejia.fincostos_backend.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record VentaResponse(
        Integer id,
        Integer fincaId,
        LocalDate fecha,
        Integer cajas,
        BigDecimal precioPorCaja,
        String cliente,
        BigDecimal total,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        UUID clientUuid) {
}
