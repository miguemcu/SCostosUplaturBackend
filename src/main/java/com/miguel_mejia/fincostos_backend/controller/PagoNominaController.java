package com.miguel_mejia.fincostos_backend.controller;

import com.miguel_mejia.fincostos_backend.dto.pago.PagoNominaPreviewResponse;
import com.miguel_mejia.fincostos_backend.dto.pago.PagoNominaRequest;
import com.miguel_mejia.fincostos_backend.dto.pago.PagoNominaResponse;
import com.miguel_mejia.fincostos_backend.entity.PagoNomina;
import com.miguel_mejia.fincostos_backend.repository.EmpleadoRepository;
import com.miguel_mejia.fincostos_backend.repository.PagoNominaRepository;
import com.miguel_mejia.fincostos_backend.service.FincaAccessService;
import com.miguel_mejia.fincostos_backend.service.PagoNominaService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fincas/{fincaId}/pagos-nomina")
@RequiredArgsConstructor
public class PagoNominaController {

    private final PagoNominaRepository pagoNominaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final FincaAccessService fincaAccessService;
    private final PagoNominaService pagoNominaService;

    @GetMapping
    public List<PagoNominaResponse> listar(
            @PathVariable Integer fincaId,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) Instant since,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        List<PagoNomina> pagos = since == null
                ? pagoNominaRepository.findByFincaIdAndFiltros(fincaId, anio, mes)
                : pagoNominaRepository.findByFincaIdUpdatedAfter(fincaId, since);
        return pagos.stream()
                .map(PagoNominaController::toResponse)
                .toList();
    }

    @GetMapping("/preview")
    public PagoNominaPreviewResponse preview(
            @PathVariable Integer fincaId,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        BigDecimal suggested = empleadoRepository.sumSalarioBaseByFincaId(fincaId);
        return new PagoNominaPreviewResponse(suggested == null ? BigDecimal.ZERO : suggested);
    }

    @PostMapping
    public ResponseEntity<PagoNominaResponse> crear(
            @PathVariable Integer fincaId,
            @Valid @RequestBody PagoNominaRequest request,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        if (request.clientUuid() != null) {
            PagoNomina existing = pagoNominaRepository.findByFincaIdAndClientUuid(fincaId, request.clientUuid())
                    .orElse(null);
            if (existing != null) {
                return ResponseEntity.ok(toResponse(existing));
            }
        }
        PagoNomina pago = pagoNominaService.crear(fincaId, request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(pago));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer fincaId,
            @PathVariable Integer id,
            Authentication authentication) {
        pagoNominaService.eliminar(fincaId, id, authentication);
        return ResponseEntity.noContent().build();
    }

    private static PagoNominaResponse toResponse(PagoNomina pago) {
        return new PagoNominaResponse(
                pago.getId(),
                pago.getFinca().getId(),
                pago.getMes(),
                pago.getFechaPago(),
                pago.getSumaSalarios(),
                pago.getDeducciones(),
                pago.getTotalPagado(),
                pago.getCreatedAt(),
                pago.getUpdatedAt(),
                pago.getDeletedAt(),
                pago.getClientUuid());
    }
}
