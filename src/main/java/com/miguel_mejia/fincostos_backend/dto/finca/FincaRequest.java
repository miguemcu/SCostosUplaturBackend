package com.miguel_mejia.fincostos_backend.dto.finca;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record FincaRequest(
        @NotBlank @Size(max = 150) String nombre,
        UUID clientUuid) {
}
