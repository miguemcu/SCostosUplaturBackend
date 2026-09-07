package com.miguel_mejia.fincostos_backend.dto.empleado;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record EmpleadoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotNull @DecimalMin(value = "0.00") BigDecimal salarioBase,
        UUID clientUuid) {
}
