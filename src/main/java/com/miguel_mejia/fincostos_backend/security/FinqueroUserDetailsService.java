package com.miguel_mejia.fincostos_backend.security;

import com.miguel_mejia.fincostos_backend.entity.Finquero;
import com.miguel_mejia.fincostos_backend.repository.FinqueroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinqueroUserDetailsService implements UserDetailsService {

    private final FinqueroRepository finqueroRepository;

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        Finquero finquero = finqueroRepository.findByUsuario(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Finquero no encontrado"));
        return FinqueroUserDetails.from(finquero);
    }
}
