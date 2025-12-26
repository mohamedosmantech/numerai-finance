package com.fincalc.adapter.out.persistence.repository;

import com.fincalc.adapter.out.persistence.entity.AnalyticsDailyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsDailyRepository extends JpaRepository<AnalyticsDailyEntity, Long> {

    Optional<AnalyticsDailyEntity> findByStatDateAndCategoryAndName(LocalDate date, String category, String name);

    List<AnalyticsDailyEntity> findByStatDateBetweenAndCategory(LocalDate startDate, LocalDate endDate, String category);

    List<AnalyticsDailyEntity> findByStatDateBetweenAndCategoryAndName(
            LocalDate startDate, LocalDate endDate, String category, String name);

    @Query("SELECT SUM(a.count) FROM AnalyticsDailyEntity a WHERE a.statDate BETWEEN :startDate AND :endDate AND a.category = :category")
    Long sumCountByDateRangeAndCategory(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") String category);

    @Query("SELECT SUM(a.count) FROM AnalyticsDailyEntity a WHERE a.statDate BETWEEN :startDate AND :endDate AND a.category = :category AND a.name = :name")
    Long sumCountByDateRangeAndCategoryAndName(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") String category,
            @Param("name") String name);

    @Query("""
        SELECT a.name, SUM(a.count) as total
        FROM AnalyticsDailyEntity a
        WHERE a.statDate BETWEEN :startDate AND :endDate AND a.category = :category
        GROUP BY a.name
        ORDER BY total DESC
        """)
    List<Object[]> findTopByCategoryInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") String category);

    @Modifying
    @Query(value = """
        INSERT INTO analytics_daily (stat_date, category, name, count)
        VALUES (:date, :category, :name, 1)
        ON CONFLICT (stat_date, category, name) DO UPDATE SET count = analytics_daily.count + 1
        """, nativeQuery = true)
    void upsertIncrement(@Param("date") LocalDate date, @Param("category") String category, @Param("name") String name);

    @Query("SELECT a FROM AnalyticsDailyEntity a WHERE a.statDate >= :startDate ORDER BY a.statDate DESC")
    List<AnalyticsDailyEntity> findRecentStats(@Param("startDate") LocalDate startDate);
}
