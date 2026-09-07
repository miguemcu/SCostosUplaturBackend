package com.miguel_mejia.fincostos_backend.controller;

import com.miguel_mejia.fincostos_backend.dto.finca.FincaRequest;
import com.miguel_mejia.fincostos_backend.dto.finca.FincaResponse;
import com.miguel_mejia.fincostos_backend.entity.Finca;
import com.miguel_mejia.fincostos_backend.repository.FincaRepository;
import com.miguel_mejia.fincostos_backend.service.FincaAccessService;
import com.miguel_mejia.fincostos_backend.service.FincaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/fincas")
@RequiredArgsConstructor
public class FincaController {

    private final FincaRepository fincaRepository;
    private final FincaAccessService fincaAccessService;
    private final FincaService fincaService;

    @GetMapping
    public List<FincaResponse> listar(
            @RequestParam(required = false) Instant since,
            Authentication authentication) {
        Integer finqueroId = fincaAccessService.getAuthenticatedFinqueroId(authentication);
        List<Finca> fincas = since == null
                ? fincaRepository.findActiveByFinqueroId(finqueroId)
                : fincaRepository.findByFinqueroIdUpdatedAfter(finqueroId, since);
        return fincas.stream()
                .map(FincaController::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<FincaResponse> crear(
            @Valid @RequestBody FincaRequest request,
            Authentication authentication) {
        Finca finca = new Finca();
        finca.setNombre(request.nombre());
        finca.setFinquero(fincaAccessService.getAuthenticatedFinquero(authentication));

        if (request.clientUuid() != null) {
            Finca existing = fincaRepository.findByFinqueroIdAndClientUuid(
                    finca.getFinquero().getId(), request.clientUuid()).orElse(null);
            if (existing != null) {
                return ResponseEntity.ok(toResponse(existing));
            }
        }
        finca.setClientUuid(request.clientUuid() == null ? UUID.randomUUID() : request.clientUuid());

        Finca saved = fincaRepository.save(finca);
        return ResponseEntity
                .created(URI.create("/fincas/" + saved.getId()))
                .body(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id,
            Authentication authentication) {
        Finca finca = fincaAccessService.requireOwnedFinca(id, authentication);
        fincaService.eliminar(finca);
        return ResponseEntity.noContent().build();
    }

    private static FincaResponse toResponse(Finca finca) {
        return new FincaResponse(
                finca.getId(),
                finca.getNombre(),
                finca.getFechaCreacion(),
                finca.getCreatedAt(),
                finca.getUpdatedAt(),
                finca.getDeletedAt(),
                finca.getClientUuid());
    }
}