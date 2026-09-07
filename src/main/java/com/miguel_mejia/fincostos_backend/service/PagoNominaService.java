package com.miguel_mejia.fincostos_backend.service;

import com.miguel_mejia.fincostos_backend.dto.pago.PagoNominaRequest;
import com.miguel_mejia.fincostos_backend.entity.Finca;
import com.miguel_mejia.fincostos_backend.entity.Gasto;
import com.miguel_mejia.fincostos_backend.entity.PagoNomina;
import com.miguel_mejia.fincostos_backend.repository.EmpleadoRepository;
import com.miguel_mejia.fincostos_backend.repository.GastoRepository;
import com.miguel_mejia.fincostos_backend.repository.PagoNominaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PagoNominaService {

    private static final String CATEGORIA_NOMINA = "Nómina";
    private static final String OBSERVACIONES_AUTOMATICAS =
            "Generado automáticamente al registrar el pago de nómina";

    private final PagoNominaRepository pagoNominaRepository;
    private final GastoRepository gastoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final FincaAccessService fincaAccessService;

    @Transactional
    public PagoNomina crear(
            Integer fincaId, PagoNominaRequest request, Authentication authentication) {
        Finca finca = fincaAccessService.requireOwnedFinca(fincaId, authentication);
        BigDecimal sumaSalarios = request.sumaSalarios() == null
                ? empleadoRepository.sumSalarioBaseByFincaId(fincaId)
                : request.sumaSalarios();
        LocalDate fechaPago = request.fechaPago() == null ? LocalDate.now() : request.fechaPago();

        PagoNomina pago = new PagoNomina();
        pago.setFinca(finca);
        pago.setMes(request.mes());
        pago.setFechaPago(fechaPago);
        pago.setSumaSalarios(sumaSalarios == null ? BigDecimal.ZERO : sumaSalarios);
        pago.setDeducciones(request.deducciones() == null ? BigDecimal.ZERO : request.deducciones());
        pago.setClientUuid(request.clientUuid() == null ? UUID.randomUUID() : request.clientUuid());

        PagoNomina saved = pagoNominaRepository.saveAndFlush(pago);
        PagoNomina persisted = pagoNominaRepository.findByIdAndFincaId(saved.getId(), fincaId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo recuperar el pago de nómina recién creado"));
        BigDecimal totalPagado = persisted.getTotalPagado();
        if (totalPagado == null) {
            totalPagado = persisted.getSumaSalarios().subtract(persisted.getDeducciones());
        }

        Gasto gasto = new Gasto();
        gasto.setFinca(finca);
        gasto.setFecha(fechaPago);
        gasto.setCategoria(CATEGORIA_NOMINA);
        gasto.setConcepto("Pago nómina " + request.mes());
        gasto.setValor(totalPagado);
        gasto.setObservaciones(OBSERVACIONES_AUTOMATICAS);
        Gasto savedGasto = gastoRepository.save(gasto);
        persisted.setGastoAsociado(savedGasto);
        pagoNominaRepository.saveAndFlush(persisted);

        return persisted;
    }

    @Transactional
    public void eliminar(
            Integer fincaId, Integer id, Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        PagoNomina pago = pagoNominaRepository.findByIdAndFincaId(id, fincaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pago de nómina no encontrado en la finca"));
        if (pago.getGastoAsociado() != null) {
            Gasto gasto = pago.getGastoAsociado();
            gasto.setDeletedAt(Instant.now());
            gastoRepository.save(gasto);
        }
        pago.setDeletedAt(Instant.now());
        pagoNominaRepository.save(pago);
    }
}