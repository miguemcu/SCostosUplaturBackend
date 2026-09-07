package com.miguel_mejia.fincostos_backend.service;

import com.miguel_mejia.fincostos_backend.entity.Empleado;
import com.miguel_mejia.fincostos_backend.entity.Finca;
import com.miguel_mejia.fincostos_backend.entity.Gasto;
import com.miguel_mejia.fincostos_backend.entity.PagoNomina;
import com.miguel_mejia.fincostos_backend.entity.Venta;
import com.miguel_mejia.fincostos_backend.repository.EmpleadoRepository;
import com.miguel_mejia.fincostos_backend.repository.FincaRepository;
import com.miguel_mejia.fincostos_backend.repository.GastoRepository;
import com.miguel_mejia.fincostos_backend.repository.PagoNominaRepository;
import com.miguel_mejia.fincostos_backend.repository.VentaRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FincaService {

    private final FincaRepository fincaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final VentaRepository ventaRepository;
    private final GastoRepository gastoRepository;
    private final PagoNominaRepository pagoNominaRepository;

    @Transactional
    public void eliminar(Finca finca) {
        Instant deletedAt = Instant.now();
        softDeleteEmpleados(empleadoRepository.findByFincaId(finca.getId()), deletedAt);
        softDeleteVentas(ventaRepository.findByFincaId(finca.getId()), deletedAt);
        softDeleteGastos(gastoRepository.findByFincaId(finca.getId()), deletedAt);
        softDeletePagos(pagoNominaRepository.findByFincaId(finca.getId()), deletedAt);
        finca.setDeletedAt(deletedAt);
        fincaRepository.save(finca);
    }

    private void softDeleteEmpleados(List<Empleado> empleados, Instant deletedAt) {
        empleados.forEach(empleado -> empleado.setDeletedAt(deletedAt));
        empleadoRepository.saveAll(empleados);
    }

    private void softDeleteVentas(List<Venta> ventas, Instant deletedAt) {
        ventas.forEach(venta -> venta.setDeletedAt(deletedAt));
        ventaRepository.saveAll(ventas);
    }

    private void softDeleteGastos(List<Gasto> gastos, Instant deletedAt) {
        gastos.forEach(gasto -> gasto.setDeletedAt(deletedAt));
        gastoRepository.saveAll(gastos);
    }

    private void softDeletePagos(List<PagoNomina> pagos, Instant deletedAt) {
        pagos.forEach(pago -> pago.setDeletedAt(deletedAt));
        pagoNominaRepository.saveAll(pagos);
    }
}