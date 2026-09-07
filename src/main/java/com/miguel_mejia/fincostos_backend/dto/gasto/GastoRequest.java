package com.miguel_mejia.fincostos_backend.dto.gasto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GastoRequest(
        LocalDate fecha,
        @NotBlank @Size(max = 100) String categoria,
        @NotBlank @Size(max = 255) String concepto,
        @NotNull @DecimalMin(value = "0.00") BigDecimal valor,
        String observaciones,
        UUID clientUuid) {
}
