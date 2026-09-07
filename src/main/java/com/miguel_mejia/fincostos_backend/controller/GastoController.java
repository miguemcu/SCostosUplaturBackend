package com.miguel_mejia.fincostos_backend.controller;

import com.miguel_mejia.fincostos_backend.dto.gasto.GastoRequest;
import com.miguel_mejia.fincostos_backend.dto.gasto.GastoResponse;
import com.miguel_mejia.fincostos_backend.entity.Finca;
import com.miguel_mejia.fincostos_backend.entity.Gasto;
import com.miguel_mejia.fincostos_backend.repository.GastoRepository;
import com.miguel_mejia.fincostos_backend.service.FincaAccessService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/fincas/{fincaId}/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoRepository gastoRepository;
    private final FincaAccessService fincaAccessService;

    @GetMapping
    public List<GastoResponse> listar(
            @PathVariable Integer fincaId,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) Instant since,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        if (since == null) {
            validateDateRange(desde, hasta);
        }
        List<Gasto> gastos = since != null
                ? gastoRepository.findByFincaIdUpdatedAfter(fincaId, since)
                : findGastos(fincaId, desde, hasta);
        return gastos.stream()
                .map(GastoController::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<GastoResponse> crear(
            @PathVariable Integer fincaId,
            @Valid @RequestBody GastoRequest request,
            Authentication authentication) {
        Finca finca = fincaAccessService.requireOwnedFinca(fincaId, authentication);
        if (request.clientUuid() != null) {
            Gasto existing = gastoRepository.findByFincaIdAndClientUuid(fincaId, request.clientUuid()).orElse(null);
            if (existing != null) {
                return ResponseEntity.ok(toResponse(existing));
            }
        }
        Gasto gasto = new Gasto();
        gasto.setFinca(finca);
        gasto.setFecha(request.fecha() == null ? LocalDate.now() : request.fecha());
        gasto.setCategoria(request.categoria());
        gasto.setConcepto(request.concepto());
        gasto.setValor(request.valor());
        gasto.setObservaciones(request.observaciones());
        gasto.setClientUuid(request.clientUuid() == null ? UUID.randomUUID() : request.clientUuid());
        Gasto saved = gastoRepository.save(gasto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer fincaId,
            @PathVariable Integer id,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        Gasto gasto = gastoRepository.findByIdAndFincaId(id, fincaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Gasto no encontrado en la finca"));
        gasto.setDeletedAt(Instant.now());
        gastoRepository.save(gasto);
        return ResponseEntity.noContent().build();
    }

    private static GastoResponse toResponse(Gasto gasto) {
        return new GastoResponse(
                gasto.getId(), gasto.getFinca().getId(), gasto.getFecha(), gasto.getCategoria(),
                gasto.getConcepto(), gasto.getValor(), gasto.getObservaciones(), gasto.getCreatedAt(),
                gasto.getUpdatedAt(), gasto.getDeletedAt(), gasto.getClientUuid());
    }

    private List<Gasto> findGastos(Integer fincaId, LocalDate desde, LocalDate hasta) {
        if (desde == null && hasta == null) {
            return gastoRepository.findByFincaIdOrderByFechaDescIdDesc(fincaId);
        }
        if (desde == null) {
            return gastoRepository.findByFincaIdAndFechaLessThanEqualOrderByFechaDescIdDesc(
                    fincaId, hasta);
        }
        if (hasta == null) {
            return gastoRepository.findByFincaIdAndFechaGreaterThanEqualOrderByFechaDescIdDesc(
                    fincaId, desde);
        }
        return gastoRepository.findByFincaIdAndFechaBetweenOrderByFechaDescIdDesc(
                fincaId, desde, hasta);
    }

    private static void validateDateRange(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La fecha desde no puede ser posterior a la fecha hasta");
        }
    }
}