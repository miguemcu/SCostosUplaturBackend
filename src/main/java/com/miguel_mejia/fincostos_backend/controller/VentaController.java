package com.miguel_mejia.fincostos_backend.controller;

import com.miguel_mejia.fincostos_backend.dto.venta.VentaRequest;
import com.miguel_mejia.fincostos_backend.dto.venta.VentaResponse;
import com.miguel_mejia.fincostos_backend.entity.Finca;
import com.miguel_mejia.fincostos_backend.entity.Venta;
import com.miguel_mejia.fincostos_backend.repository.VentaRepository;
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
@RequestMapping("/fincas/{fincaId}/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaRepository ventaRepository;
    private final FincaAccessService fincaAccessService;

    @GetMapping
    public List<VentaResponse> listar(
            @PathVariable Integer fincaId,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) Instant since,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        if (since == null) {
            validateDateRange(desde, hasta);
        }
        List<Venta> ventas = since != null
                ? ventaRepository.findByFincaIdUpdatedAfter(fincaId, since)
                : findVentas(fincaId, desde, hasta);
        return ventas.stream()
                .map(VentaController::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<VentaResponse> crear(
            @PathVariable Integer fincaId,
            @Valid @RequestBody VentaRequest request,
            Authentication authentication) {
        Finca finca = fincaAccessService.requireOwnedFinca(fincaId, authentication);
        if (request.clientUuid() != null) {
            Venta existing = ventaRepository.findByFincaIdAndClientUuid(fincaId, request.clientUuid()).orElse(null);
            if (existing != null) {
                return ResponseEntity.ok(toResponse(existing));
            }
        }
        Venta venta = new Venta();
        venta.setFinca(finca);
        venta.setCajas(request.cajas());
        venta.setPrecioPorCaja(request.precioPorCaja());
        venta.setCliente(request.cliente());
        venta.setClientUuid(request.clientUuid() == null ? UUID.randomUUID() : request.clientUuid());
        Venta saved = ventaRepository.save(venta);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer fincaId,
            @PathVariable Integer id,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        Venta venta = ventaRepository.findByIdAndFincaId(id, fincaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Venta no encontrada en la finca"));
        venta.setDeletedAt(Instant.now());
        ventaRepository.save(venta);
        return ResponseEntity.noContent().build();
    }

    private static VentaResponse toResponse(Venta venta) {
        return new VentaResponse(
                venta.getId(), venta.getFinca().getId(), venta.getFecha(), venta.getCajas(),
                venta.getPrecioPorCaja(), venta.getCliente(), venta.getTotal(), venta.getCreatedAt(),
                venta.getUpdatedAt(), venta.getDeletedAt(), venta.getClientUuid());
    }

    private List<Venta> findVentas(Integer fincaId, LocalDate desde, LocalDate hasta) {
        if (desde == null && hasta == null) {
            return ventaRepository.findByFincaIdOrderByFechaDescIdDesc(fincaId);
        }
        if (desde == null) {
            return ventaRepository.findByFincaIdAndFechaLessThanEqualOrderByFechaDescIdDesc(
                    fincaId, hasta);
        }
        if (hasta == null) {
            return ventaRepository.findByFincaIdAndFechaGreaterThanEqualOrderByFechaDescIdDesc(
                    fincaId, desde);
        }
        return ventaRepository.findByFincaIdAndFechaBetweenOrderByFechaDescIdDesc(
                fincaId, desde, hasta);
    }

    private static void validateDateRange(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La fecha desde no puede ser posterior a la fecha hasta");
        }
    }
}