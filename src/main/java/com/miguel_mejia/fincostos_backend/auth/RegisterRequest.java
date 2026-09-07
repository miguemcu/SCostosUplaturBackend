package com.miguel_mejia.fincostos_backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Size(max = 100) String usuario,
        @NotBlank @Size(min = 8, max = 255) String clave) {
}
