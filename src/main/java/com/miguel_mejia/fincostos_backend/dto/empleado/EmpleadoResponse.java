package com.miguel_mejia.fincostos_backend.dto.empleado;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EmpleadoResponse(
        Integer id,
        Integer fincaId,
        String nombre,
        BigDecimal salarioBase,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        UUID clientUuid) {
}
