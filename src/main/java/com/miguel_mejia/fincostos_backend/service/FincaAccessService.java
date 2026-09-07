package com.miguel_mejia.fincostos_backend.service;

import com.miguel_mejia.fincostos_backend.entity.Finca;
import com.miguel_mejia.fincostos_backend.entity.Finquero;
import com.miguel_mejia.fincostos_backend.repository.FincaRepository;
import com.miguel_mejia.fincostos_backend.repository.FinqueroRepository;
import com.miguel_mejia.fincostos_backend.security.FinqueroUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FincaAccessService {

    private final FincaRepository fincaRepository;
    private final FinqueroRepository finqueroRepository;

    public Integer getAuthenticatedFinqueroId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof FinqueroUserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Finquero no autenticado");
        }
        return userDetails.getFinqueroId();
    }

    public Finquero getAuthenticatedFinquero(Authentication authentication) {
        Integer finqueroId = getAuthenticatedFinqueroId(authentication);
        return finqueroRepository.findById(finqueroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Finquero no válido"));
    }

    public Finca requireOwnedFinca(Integer fincaId, Authentication authentication) {
        Integer finqueroId = getAuthenticatedFinqueroId(authentication);
        return fincaRepository.findById(fincaId)
                .filter(finca -> finca.getFinquero().getId().equals(finqueroId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "La finca no pertenece al finquero autenticado"));
    }
}
