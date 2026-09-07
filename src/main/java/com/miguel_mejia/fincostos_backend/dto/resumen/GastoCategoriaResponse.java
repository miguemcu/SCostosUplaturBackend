package com.miguel_mejia.fincostos_backend.dto.resumen;

import java.math.BigDecimal;

public record GastoCategoriaResponse(
        String categoria,
        BigDecimal total) {
}