package com.miguel_mejia.fincostos_backend.dto.finca;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FincaRequest(
        @NotBlank @Size(max = 150) String nombre) {
}
