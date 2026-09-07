package com.miguel_mejia.fincostos_backend.controller;

import com.miguel_mejia.fincostos_backend.dto.empleado.EmpleadoRequest;
import com.miguel_mejia.fincostos_backend.dto.empleado.EmpleadoResponse;
import com.miguel_mejia.fincostos_backend.dto.empleado.EmpleadoResumenResponse;
import com.miguel_mejia.fincostos_backend.entity.Empleado;
import com.miguel_mejia.fincostos_backend.entity.Finca;
import com.miguel_mejia.fincostos_backend.repository.EmpleadoRepository;
import com.miguel_mejia.fincostos_backend.service.FincaAccessService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/fincas/{fincaId}/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoRepository empleadoRepository;
    private final FincaAccessService fincaAccessService;

    @GetMapping
    public List<EmpleadoResponse> listar(
            @PathVariable Integer fincaId,
            @RequestParam(required = false) Instant since,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        List<Empleado> empleados = since == null
                ? empleadoRepository.findByFincaId(fincaId)
                : empleadoRepository.findByFincaIdUpdatedAfter(fincaId, since);
        return empleados.stream()
                .map(EmpleadoController::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<EmpleadoResponse> crear(
            @PathVariable Integer fincaId,
            @Valid @RequestBody EmpleadoRequest request,
            Authentication authentication) {
        Finca finca = fincaAccessService.requireOwnedFinca(fincaId, authentication);
        if (request.clientUuid() != null) {
            Empleado existing = empleadoRepository.findByFincaIdAndClientUuid(fincaId, request.clientUuid()).orElse(null);
            if (existing != null) {
                return ResponseEntity.ok(toResponse(existing));
            }
        }
        Empleado empleado = new Empleado();
        empleado.setFinca(finca);
        empleado.setClientUuid(request.clientUuid() == null ? UUID.randomUUID() : request.clientUuid());
        updateFields(empleado, request);

        Empleado saved = empleadoRepository.save(empleado);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public EmpleadoResponse editar(
            @PathVariable Integer fincaId,
            @PathVariable Integer id,
            @Valid @RequestBody EmpleadoRequest request,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        Empleado empleado = findEmployee(id, fincaId);
        updateFields(empleado, request);
        return toResponse(empleadoRepository.save(empleado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Integer fincaId,
            @PathVariable Integer id,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        Empleado empleado = findEmployee(id, fincaId);
        empleado.setDeletedAt(Instant.now());
        empleadoRepository.save(empleado);
    }

    @GetMapping("/resumen")
    public EmpleadoResumenResponse resumen(
            @PathVariable Integer fincaId,
            Authentication authentication) {
        fincaAccessService.requireOwnedFinca(fincaId, authentication);
        List<EmpleadoResponse> empleados = empleadoRepository.findByFincaId(fincaId).stream()
                .map(EmpleadoController::toResponse)
                .toList();
        BigDecimal total = empleados.stream()
                .map(EmpleadoResponse::salarioBase)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new EmpleadoResumenResponse(empleados, total);
    }

    private Empleado findEmployee(Integer id, Integer fincaId) {
        return empleadoRepository.findByIdAndFincaId(id, fincaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Empleado no encontrado en la finca"));
    }

    private static void updateFields(Empleado empleado, EmpleadoRequest request) {
        empleado.setNombre(request.nombre());
        empleado.setSalarioBase(request.salarioBase());
    }

    private static EmpleadoResponse toResponse(Empleado empleado) {
        return new EmpleadoResponse(
                empleado.getId(),
                empleado.getFinca().getId(),
                empleado.getNombre(),
                empleado.getSalarioBase(),
                empleado.getCreatedAt(),
                empleado.getUpdatedAt(),
                empleado.getDeletedAt(),
                empleado.getClientUuid());
    }
}