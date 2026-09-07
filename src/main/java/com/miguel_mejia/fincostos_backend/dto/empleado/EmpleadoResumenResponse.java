package com.miguel_mejia.fincostos_backend.dto.empleado;

import java.math.BigDecimal;
import java.util.List;

public record EmpleadoResumenResponse(
        List<EmpleadoResponse> empleados,
        BigDecimal totalSalariosBase) {
}
