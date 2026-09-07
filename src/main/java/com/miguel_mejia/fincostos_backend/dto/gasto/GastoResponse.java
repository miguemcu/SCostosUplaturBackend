package com.miguel_mejia.fincostos_backend.dto.gasto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record GastoResponse(
        Integer id,
        Integer fincaId,
        LocalDate fecha,
        String categoria,
        String concepto,
        BigDecimal valor,
        String observaciones,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        UUID clientUuid) {
}
