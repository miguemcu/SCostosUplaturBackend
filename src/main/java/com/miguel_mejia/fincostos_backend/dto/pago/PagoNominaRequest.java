package com.miguel_mejia.fincostos_backend.dto.pago;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagoNominaRequest(
        @NotBlank @Size(max = 50) String mes,
        LocalDate fechaPago,
        @DecimalMin(value = "0.00") BigDecimal sumaSalarios,
        @DecimalMin(value = "0.00") BigDecimal deducciones,
        UUID clientUuid) {
}