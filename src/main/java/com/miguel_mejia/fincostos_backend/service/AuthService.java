package com.miguel_mejia.fincostos_backend.service;

import com.miguel_mejia.fincostos_backend.auth.LoginRequest;
import com.miguel_mejia.fincostos_backend.auth.LoginResponse;
import com.miguel_mejia.fincostos_backend.auth.RegisterRequest;
import com.miguel_mejia.fincostos_backend.entity.Finquero;
import com.miguel_mejia.fincostos_backend.repository.FinqueroRepository;
import com.miguel_mejia.fincostos_backend.security.FinqueroUserDetails;
import com.miguel_mejia.fincostos_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final FinqueroRepository finqueroRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.usuario(), request.clave()));
        FinqueroUserDetails userDetails = (FinqueroUserDetails) authentication.getPrincipal();
        return new LoginResponse(jwtService.generateToken(userDetails));
    }

    public LoginResponse register(RegisterRequest request) {
        if (finqueroRepository.findByUsuario(request.usuario()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        }

        Finquero finquero = new Finquero();
        finquero.setNombre(request.nombre());
        finquero.setUsuario(request.usuario());
        finquero.setClave(passwordEncoder.encode(request.clave()));
        Finquero saved = finqueroRepository.save(finquero);

        return new LoginResponse(jwtService.generateToken(FinqueroUserDetails.from(saved)));
    }
}
