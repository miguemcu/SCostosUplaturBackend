package com.miguel_mejia.fincostos_backend.repository;

import com.miguel_mejia.fincostos_backend.entity.Finca;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FincaRepository extends JpaRepository<Finca, Integer> {

    List<Finca> findByFinqueroId(Integer finqueroId);

    List<Finca> findByFinqueroIdIn(Collection<Integer> finqueroIds);
}
