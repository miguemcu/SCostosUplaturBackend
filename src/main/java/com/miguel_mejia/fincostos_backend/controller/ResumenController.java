package com.miguel_mejia.fincostos_backend.controller;

import com.miguel_mejia.fincostos_backend.dto.resumen.GastoCategoriaResponse;
import com.miguel_mejia.fincostos_backend.dto.resumen.ResumenAnualResponse;
import com.miguel_mejia.fincostos_backend.dto.resumen.ResumenMensualResponse;
import com.miguel_mejia.fincostos_backend.repository.CategoryAggregate;
import com.miguel_mejia.fincostos_backend.repository.ExpenseSummaryAggregate;
import com.miguel_mejia.fincostos_backend.repository.GastoRepository;
import com.miguel_mejia.fincostos_backend.repository.MonthlyExpenseAggregate;
import com.miguel_mejia.fincostos_backend.repository.MonthlySalesAggregate;
import com.miguel_mejia.fincostos_backend.repository.SummaryAggregate;
import com.miguel_mejia.fincostos_backend.repository.VentaRepository;
import com.miguel_mejia.fincostos_backend.service.FincaAccessService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fincas/{fincaId}/resumen")
@RequiredArgsConstructor
public class ResumenController {

    private final VentaRepository ventaRepository;
    private final GastoRepository gastoRepository;
    private final FincaAccessService fincaAccessService;

    @GetMapping("/mensual")
    public ResumenMensualResponse mensual(
            @PathVariable Integer fincaId,
            @RequestParam int anio,
            @RequestParam int mes,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        PeriodData current = loadPeriod(fincaId, desde, hasta);
        LocalDate previousDate = desde.minusMonths(1);
        PeriodData previous = loadPeriod(
                fincaId,
                previousDate.withDayOfMonth(1),
                previousDate.withDayOfMonth(previousDate.lengthOfMonth()));
        return toMonthlyResponse(anio, mes, current, variation(current, previous), true);
    }

    @GetMapping("/anual")
    public ResumenAnualResponse anual(
            @PathVariable Integer fincaId,
            @RequestParam int anio,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        LocalDate desde = LocalDate.of(anio, 1, 1);
        LocalDate hasta = LocalDate.of(anio, 12, 31);
        PeriodData current = loadPeriod(fincaId, desde, hasta);
        PeriodData previous = loadPeriod(
                fincaId,
                LocalDate.of(anio - 1, 1, 1),
                LocalDate.of(anio - 1, 12, 31));

        Map<Integer, MonthlySalesAggregate> salesByMonth = ventaRepository
                .sumByMonth(fincaId, desde, hasta).stream()
                .collect(Collectors.toMap(MonthlySalesAggregate::getMes, Function.identity()));
        Map<Integer, MonthlyExpenseAggregate> expensesByMonth = gastoRepository
                .sumByMonth(fincaId, desde, hasta).stream()
                .collect(Collectors.toMap(MonthlyExpenseAggregate::getMes, Function.identity()));
        List<ResumenMensualResponse> months = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(month -> toMonthlyResponse(
                        anio,
                        month,
                        salesByMonth.get(month),
                        expensesByMonth.get(month)))
                .toList();

        return new ResumenAnualResponse(
                anio,
                current.ingresos(),
                current.gastos(),
                current.resultado(),
                current.cajasVendidas(),
                current.costoPromedioPorCaja(),
                current.precioPromedioVentaPorCaja(),
                current.utilidadPorCaja(),
                current.margenGanancia(),
                current.gastosPorCategoria(),
                variation(current, previous),
                months);
    }

    private PeriodData loadPeriod(Integer fincaId, LocalDate desde, LocalDate hasta) {
        SummaryAggregate sales = ventaRepository.sumByFincaIdAndFechaBetween(fincaId, desde, hasta);
        ExpenseSummaryAggregate expenses = gastoRepository
                .sumByFincaIdAndFechaBetween(fincaId, desde, hasta);
        BigDecimal ingresos = valueOrZero(sales == null ? null : sales.getTotal());
        BigDecimal gastos = valueOrZero(expenses == null ? null : expenses.getTotal());
        BigDecimal cajas = valueOrZero(sales == null ? null : sales.getTotalCajas());
        return new PeriodData(
                ingresos,
                gastos,
                cajas,
                calculateMetrics(ingresos, gastos, cajas),
                toCategories(gastoRepository.sumByCategoria(fincaId, desde, hasta)),
                count(sales) + count(expenses));
    }

    private static ResumenMensualResponse toMonthlyResponse(
            int anio,
            int mes,
            PeriodData period,
            BigDecimal variation,
            boolean includeCategories) {
        return new ResumenMensualResponse(
                anio,
                mes,
                period.ingresos(),
                period.gastos(),
                period.resultado(),
                period.cajasVendidas(),
                period.costoPromedioPorCaja(),
                period.precioPromedioVentaPorCaja(),
                period.utilidadPorCaja(),
                period.margenGanancia(),
                includeCategories ? period.gastosPorCategoria() : List.of(),
                variation);
    }

    private static ResumenMensualResponse toMonthlyResponse(
            int anio,
            int mes,
            MonthlySalesAggregate sales,
            MonthlyExpenseAggregate expenses) {
        BigDecimal ingresos = valueOrZero(sales == null ? null : sales.getTotal());
        BigDecimal gastos = valueOrZero(expenses == null ? null : expenses.getTotal());
        BigDecimal cajas = valueOrZero(sales == null ? null : sales.getTotalCajas());
        Metrics metrics = calculateMetrics(ingresos, gastos, cajas);
        return new ResumenMensualResponse(
                anio,
                mes,
                ingresos,
                gastos,
                metrics.resultado(),
                cajas,
                metrics.costoPromedioPorCaja(),
                metrics.precioPromedioVentaPorCaja(),
                metrics.utilidadPorCaja(),
                metrics.margenGanancia(),
                List.of(),
                null);
    }

    private static Metrics calculateMetrics(BigDecimal ingresos, BigDecimal gastos, BigDecimal cajas) {
        BigDecimal resultado = ingresos.subtract(gastos);
        BigDecimal costo = average(gastos, cajas);
        BigDecimal precio = average(ingresos, cajas);
        BigDecimal utilidad = costo == null || precio == null ? null : precio.subtract(costo);
        BigDecimal margen = ingresos.signum() == 0
                ? null
                : resultado.multiply(BigDecimal.valueOf(100))
                        .divide(ingresos, 2, RoundingMode.HALF_UP);
        return new Metrics(resultado, costo, precio, utilidad, margen);
    }

    private static BigDecimal variation(PeriodData current, PeriodData previous) {
        return previous.count() == 0 || previous.resultado().signum() == 0
                ? null
                : current.resultado().subtract(previous.resultado())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(previous.resultado().abs(), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal average(BigDecimal value, BigDecimal divisor) {
        return divisor.signum() == 0 ? null : value.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    private static List<GastoCategoriaResponse> toCategories(List<CategoryAggregate> categories) {
        return categories.stream()
                .map(category -> new GastoCategoriaResponse(category.getCategoria(), category.getTotal()))
                .toList();
    }

    private static long count(SummaryAggregate aggregate) {
        return aggregate == null || aggregate.getTotalRegistros() == null
            ? 0 : aggregate.getTotalRegistros();
    }

    private static long count(ExpenseSummaryAggregate aggregate) {
        return aggregate == null || aggregate.getTotalRegistros() == null
            ? 0 : aggregate.getTotalRegistros();
    }

    private static BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record Metrics(
            BigDecimal resultado,
            BigDecimal costoPromedioPorCaja,
            BigDecimal precioPromedioVentaPorCaja,
            BigDecimal utilidadPorCaja,
            BigDecimal margenGanancia) {
    }

    private record PeriodData(
            BigDecimal ingresos,
            BigDecimal gastos,
            BigDecimal cajasVendidas,
            Metrics metrics,
            List<GastoCategoriaResponse> gastosPorCategoria,
            long count) {

        private BigDecimal resultado() {
            return metrics.resultado();
        }

        private BigDecimal costoPromedioPorCaja() {
            return metrics.costoPromedioPorCaja();
        }

        private BigDecimal precioPromedioVentaPorCaja() {
            return metrics.precioPromedioVentaPorCaja();
        }

        private BigDecimal utilidadPorCaja() {
            return metrics.utilidadPorCaja();
        }

        private BigDecimal margenGanancia() {
            return metrics.margenGanancia();
        }
    }
}
