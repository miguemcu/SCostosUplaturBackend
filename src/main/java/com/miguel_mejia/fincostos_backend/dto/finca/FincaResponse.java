package com.miguel_mejia.fincostos_backend.dto.finca;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record FincaResponse(
        Integer id,
        String nombre,
        LocalDate fechaCreacion,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        UUID clientUuid) {
}
