package com.miguel_mejia.fincostos_backend.dto.pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record PagoNominaResponse(
        Integer id,
        Integer fincaId,
        String mes,
        LocalDate fechaPago,
        BigDecimal sumaSalarios,
        BigDecimal deducciones,
        BigDecimal totalPagado,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        UUID clientUuid) {
}