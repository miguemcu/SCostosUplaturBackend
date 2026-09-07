package com.miguel_mejia.fincostos_backend.repository;

import com.miguel_mejia.fincostos_backend.entity.PagoNomina;
import java.util.Collection;
import java.util.List;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoNominaRepository extends JpaRepository<PagoNomina, Integer> {

    @Query("SELECT p FROM PagoNomina p WHERE p.finca.id = :fincaId AND p.deletedAt IS NULL")
    List<PagoNomina> findByFincaId(@Param("fincaId") Integer fincaId);

    List<PagoNomina> findByFincaIdIn(Collection<Integer> fincaIds);

        @Query("""
            SELECT p FROM PagoNomina p
                        WHERE p.finca.id = :fincaId
                            AND p.deletedAt IS NULL
              AND (:anio IS NULL OR YEAR(p.fechaPago) = :anio)
              AND (:mes IS NULL OR p.mes = :mes)
            ORDER BY p.fechaPago DESC, p.id DESC
            """)
        List<PagoNomina> findByFincaIdAndFiltros(
            @Param("fincaId") Integer fincaId,
            @Param("anio") Integer anio,
            @Param("mes") String mes);

        @Query("SELECT p FROM PagoNomina p WHERE p.id = :id AND p.finca.id = :fincaId AND p.deletedAt IS NULL")
        Optional<PagoNomina> findByIdAndFincaId(
            @Param("id") Integer id, @Param("fincaId") Integer fincaId);

        @Query("SELECT p FROM PagoNomina p WHERE p.finca.id = :fincaId AND p.updatedAt > :since ORDER BY p.updatedAt ASC, p.id ASC")
        List<PagoNomina> findByFincaIdUpdatedAfter(@Param("fincaId") Integer fincaId, @Param("since") Instant since);

        Optional<PagoNomina> findByFincaIdAndClientUuid(Integer fincaId, UUID clientUuid);
}
