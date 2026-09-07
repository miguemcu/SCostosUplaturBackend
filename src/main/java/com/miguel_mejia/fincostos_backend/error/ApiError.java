package com.miguel_mejia.fincostos_backend.error;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime fecha,
        int estado,
        String mensaje,
        String ruta,
        List<String> detalles) {
}