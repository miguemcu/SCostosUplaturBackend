package com.miguel_mejia.fincostos_backend.repository;

import com.miguel_mejia.fincostos_backend.entity.Empleado;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    @Query("SELECT e FROM Empleado e WHERE e.finca.id = :fincaId AND e.deletedAt IS NULL")
    List<Empleado> findByFincaId(@Param("fincaId") Integer fincaId);

    List<Empleado> findByFincaIdIn(Collection<Integer> fincaIds);

        @Query("SELECT e FROM Empleado e WHERE e.id = :id AND e.finca.id = :fincaId AND e.deletedAt IS NULL")
        Optional<Empleado> findByIdAndFincaId(
            @Param("id") Integer id, @Param("fincaId") Integer fincaId);

    @Query("SELECT e FROM Empleado e WHERE e.finca.id = :fincaId AND e.updatedAt > :since ORDER BY e.updatedAt ASC, e.id ASC")
    List<Empleado> findByFincaIdUpdatedAfter(@Param("fincaId") Integer fincaId, @Param("since") Instant since);

    Optional<Empleado> findByFincaIdAndClientUuid(Integer fincaId, UUID clientUuid);

    @Query("SELECT COALESCE(SUM(e.salarioBase), 0) FROM Empleado e WHERE e.finca.id = :fincaId AND e.deletedAt IS NULL")
    BigDecimal sumSalarioBaseByFincaId(@Param("fincaId") Integer fincaId);
}
