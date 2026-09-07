package com.miguel_mejia.fincostos_backend.dto.finca;

import java.time.LocalDate;

public record FincaResponse(
        Integer id,
        String nombre,
        LocalDate fechaCreacion) {
}
