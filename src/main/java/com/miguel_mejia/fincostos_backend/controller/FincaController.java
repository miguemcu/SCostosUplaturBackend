package com.miguel_mejia.fincostos_backend.controller;

import com.miguel_mejia.fincostos_backend.dto.finca.FincaRequest;
import com.miguel_mejia.fincostos_backend.dto.finca.FincaResponse;
import com.miguel_mejia.fincostos_backend.entity.Finca;
import com.miguel_mejia.fincostos_backend.repository.FincaRepository;
import com.miguel_mejia.fincostos_backend.service.FincaAccessService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fincas")
@RequiredArgsConstructor
public class FincaController {

    private final FincaRepository fincaRepository;
    private final FincaAccessService fincaAccessService;

    @GetMapping
    public List<FincaResponse> listar(Authentication authentication) {
        Integer finqueroId = fincaAccessService.getAuthenticatedFinqueroId(authentication);
        return fincaRepository.findByFinqueroId(finqueroId).stream()
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
        fincaRepository.delete(finca);
        return ResponseEntity.noContent().build();
    }

    private static FincaResponse toResponse(Finca finca) {
        return new FincaResponse(finca.getId(), finca.getNombre(), finca.getFechaCreacion());
    }
}