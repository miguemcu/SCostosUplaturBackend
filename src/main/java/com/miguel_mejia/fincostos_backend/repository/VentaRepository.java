package com.miguel_mejia.fincostos_backend.repository;

import com.miguel_mejia.fincostos_backend.entity.Venta;
import java.util.Collection;
import java.util.List;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    @Query("SELECT v FROM Venta v WHERE v.finca.id = :fincaId AND v.deletedAt IS NULL")
    List<Venta> findByFincaId(@Param("fincaId") Integer fincaId);

        @Query("SELECT v FROM Venta v WHERE v.finca.id = :fincaId AND v.deletedAt IS NULL ORDER BY v.fecha DESC, v.id DESC")
        List<Venta> findByFincaIdOrderByFechaDescIdDesc(@Param("fincaId") Integer fincaId);

        @Query("SELECT v FROM Venta v WHERE v.finca.id = :fincaId AND v.deletedAt IS NULL AND v.fecha >= :desde ORDER BY v.fecha DESC, v.id DESC")
        List<Venta> findByFincaIdAndFechaGreaterThanEqualOrderByFechaDescIdDesc(
            @Param("fincaId") Integer fincaId, @Param("desde") LocalDate desde);

        @Query("SELECT v FROM Venta v WHERE v.finca.id = :fincaId AND v.deletedAt IS NULL AND v.fecha <= :hasta ORDER BY v.fecha DESC, v.id DESC")
        List<Venta> findByFincaIdAndFechaLessThanEqualOrderByFechaDescIdDesc(
            @Param("fincaId") Integer fincaId, @Param("hasta") LocalDate hasta);

    List<Venta> findByFincaIdIn(Collection<Integer> fincaIds);

        @Query("SELECT v FROM Venta v WHERE v.finca.id = :fincaId AND v.deletedAt IS NULL AND v.fecha BETWEEN :desde AND :hasta ORDER BY v.fecha DESC, v.id DESC")
        List<Venta> findByFincaIdAndFechaBetweenOrderByFechaDescIdDesc(
            @Param("fincaId") Integer fincaId, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

        @Query("SELECT v FROM Venta v WHERE v.finca.id = :fincaId AND v.updatedAt > :since ORDER BY v.updatedAt ASC, v.id ASC")
        List<Venta> findByFincaIdUpdatedAfter(@Param("fincaId") Integer fincaId, @Param("since") Instant since);

        Optional<Venta> findByFincaIdAndClientUuid(Integer fincaId, UUID clientUuid);

        @Query("""
            SELECT COALESCE(SUM(v.total), 0) AS total,
                    COALESCE(SUM(v.cajas), 0) AS totalCajas,
                       COUNT(v.id) AS totalRegistros
            FROM Venta v
            WHERE v.finca.id = :fincaId
              AND v.fecha BETWEEN :desde AND :hasta
            """)
        SummaryAggregate sumByFincaIdAndFechaBetween(
            @Param("fincaId") Integer fincaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

        @Query("SELECT v FROM Venta v WHERE v.id = :id AND v.finca.id = :fincaId AND v.deletedAt IS NULL")
        java.util.Optional<Venta> findByIdAndFincaId(
            @Param("id") Integer id, @Param("fincaId") Integer fincaId);

        @Query(value = """
            SELECT EXTRACT(MONTH FROM fecha)::integer AS mes,
               COALESCE(SUM(total), 0) AS total,
               COALESCE(SUM(cajas), 0) AS totalCajas
            FROM venta
            WHERE finca_id = :fincaId
              AND fecha BETWEEN :desde AND :hasta
            GROUP BY EXTRACT(MONTH FROM fecha)
            """, nativeQuery = true)
        List<MonthlySalesAggregate> sumByMonth(
            @Param("fincaId") Integer fincaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
