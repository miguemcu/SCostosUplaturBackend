package com.miguel_mejia.fincostos_backend.dto.venta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record VentaRequest(
        @NotNull @Min(1) Integer cajas,
        @NotNull @DecimalMin(value = "0.00") BigDecimal precioPorCaja,
        @Size(max = 150) String cliente,
        UUID clientUuid) {
}
