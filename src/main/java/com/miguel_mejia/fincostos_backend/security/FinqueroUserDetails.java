package com.miguel_mejia.fincostos_backend.security;

import com.miguel_mejia.fincostos_backend.entity.Finquero;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class FinqueroUserDetails implements UserDetails {

    private final Integer finqueroId;
    private final String usuario;
    private final String clave;

    private FinqueroUserDetails(Integer finqueroId, String usuario, String clave) {
        this.finqueroId = finqueroId;
        this.usuario = usuario;
        this.clave = clave;
    }

    public static FinqueroUserDetails from(Finquero finquero) {
        return new FinqueroUserDetails(finquero.getId(), finquero.getUsuario(), finquero.getClave());
    }

    public Integer getFinqueroId() {
        return finqueroId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return usuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
