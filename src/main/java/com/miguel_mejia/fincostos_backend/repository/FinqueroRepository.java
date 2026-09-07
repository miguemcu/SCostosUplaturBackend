package com.miguel_mejia.fincostos_backend.repository;

import com.miguel_mejia.fincostos_backend.entity.Finquero;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinqueroRepository extends JpaRepository<Finquero, Integer> {

    Optional<Finquero> findByUsuario(String usuario);
}
