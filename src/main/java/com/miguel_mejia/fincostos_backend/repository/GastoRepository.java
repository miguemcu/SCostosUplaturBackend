package com.miguel_mejia.fincostos_backend.repository;

import com.miguel_mejia.fincostos_backend.entity.Gasto;
import java.util.Collection;
import java.util.List;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GastoRepository extends JpaRepository<Gasto, Integer> {

    @Query("SELECT g FROM Gasto g WHERE g.finca.id = :fincaId AND g.deletedAt IS NULL")
    List<Gasto> findByFincaId(@Param("fincaId") Integer fincaId);

        @Query("SELECT g FROM Gasto g WHERE g.finca.id = :fincaId AND g.deletedAt IS NULL ORDER BY g.fecha DESC, g.id DESC")
        List<Gasto> findByFincaIdOrderByFechaDescIdDesc(@Param("fincaId") Integer fincaId);

    List<Gasto> findByFincaIdIn(Collection<Integer> fincaIds);

        @Query("SELECT g FROM Gasto g WHERE g.finca.id = :fincaId AND g.deletedAt IS NULL AND g.fecha >= :desde ORDER BY g.fecha DESC, g.id DESC")
        List<Gasto> findByFincaIdAndFechaGreaterThanEqualOrderByFechaDescIdDesc(
          @Param("fincaId") Integer fincaId, @Param("desde") LocalDate desde);

        @Query("SELECT g FROM Gasto g WHERE g.finca.id = :fincaId AND g.deletedAt IS NULL AND g.fecha <= :hasta ORDER BY g.fecha DESC, g.id DESC")
        List<Gasto> findByFincaIdAndFechaLessThanEqualOrderByFechaDescIdDesc(
          @Param("fincaId") Integer fincaId, @Param("hasta") LocalDate hasta);

        @Query("SELECT g FROM Gasto g WHERE g.finca.id = :fincaId AND g.deletedAt IS NULL AND g.fecha BETWEEN :desde AND :hasta ORDER BY g.fecha DESC, g.id DESC")
        List<Gasto> findByFincaIdAndFechaBetweenOrderByFechaDescIdDesc(
          @Param("fincaId") Integer fincaId, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

        @Query("SELECT g FROM Gasto g WHERE g.finca.id = :fincaId AND g.updatedAt > :since ORDER BY g.updatedAt ASC, g.id ASC")
        List<Gasto> findByFincaIdUpdatedAfter(@Param("fincaId") Integer fincaId, @Param("since") Instant since);

        Optional<Gasto> findByFincaIdAndClientUuid(Integer fincaId, UUID clientUuid);

        @Query("""
                     SELECT COALESCE(SUM(g.valor), 0) AS total,
                       COUNT(g.id) AS totalRegistros
            FROM Gasto g
            WHERE g.finca.id = :fincaId
              AND g.fecha BETWEEN :desde AND :hasta
            """)
        ExpenseSummaryAggregate sumByFincaIdAndFechaBetween(
            @Param("fincaId") Integer fincaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

        @Query("SELECT g FROM Gasto g WHERE g.id = :id AND g.finca.id = :fincaId AND g.deletedAt IS NULL")
        java.util.Optional<Gasto> findByIdAndFincaId(
          @Param("id") Integer id, @Param("fincaId") Integer fincaId);

              @Query("""
                SELECT g.categoria AS categoria, SUM(g.valor) AS total
                FROM Gasto g
                WHERE g.finca.id = :fincaId
                  AND g.fecha BETWEEN :desde AND :hasta
                GROUP BY g.categoria
                ORDER BY g.categoria
                """)
              List<CategoryAggregate> sumByCategoria(
                @Param("fincaId") Integer fincaId,
                @Param("desde") LocalDate desde,
                @Param("hasta") LocalDate hasta);

        @Query(value = """
            SELECT EXTRACT(MONTH FROM fecha)::integer AS mes,
               COALESCE(SUM(valor), 0) AS total
            FROM gasto
            WHERE finca_id = :fincaId
              AND fecha BETWEEN :desde AND :hasta
            GROUP BY EXTRACT(MONTH FROM fecha)
            """, nativeQuery = true)
        List<MonthlyExpenseAggregate> sumByMonth(
            @Param("fincaId") Integer fincaId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
