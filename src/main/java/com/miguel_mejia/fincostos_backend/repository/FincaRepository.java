package com.miguel_mejia.fincostos_backend.repository;

import com.miguel_mejia.fincostos_backend.entity.Finca;
import java.util.Collection;
import java.util.List;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FincaRepository extends JpaRepository<Finca, Integer> {

        @Query("SELECT f FROM Finca f WHERE f.finquero.id = :finqueroId AND f.deletedAt IS NULL")
        List<Finca> findActiveByFinqueroId(@Param("finqueroId") Integer finqueroId);

        @Query("SELECT f FROM Finca f WHERE f.finquero.id = :finqueroId AND f.updatedAt > :since ORDER BY f.updatedAt ASC, f.id ASC")
        List<Finca> findByFinqueroIdUpdatedAfter(
            @Param("finqueroId") Integer finqueroId, @Param("since") Instant since);

        Optional<Finca> findByFinqueroIdAndClientUuid(Integer finqueroId, UUID clientUuid);

    List<Finca> findByFinqueroIdIn(Collection<Integer> finqueroIds);
}
