package com.miguel_mejia.fincostos_backend.dto.resumen;

import java.math.BigDecimal;
import java.util.List;

public record ResumenAnualResponse(
        int anio,
        BigDecimal ingresos,
        BigDecimal gastos,
        BigDecimal resultado,
        BigDecimal cajasVendidas,
        BigDecimal costoPromedioPorCaja,
        BigDecimal precioPromedioVentaPorCaja,
        BigDecimal utilidadPorCaja,
        BigDecimal margenGanancia,
        List<GastoCategoriaResponse> gastosPorCategoria,
        BigDecimal variacionResultado,
        List<ResumenMensualResponse> meses) {
}
