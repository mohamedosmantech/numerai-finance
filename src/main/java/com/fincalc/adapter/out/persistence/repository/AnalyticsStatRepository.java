package com.fincalc.adapter.out.persistence.repository;

import com.fincalc.adapter.out.persistence.entity.AnalyticsStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsStatRepository extends JpaRepository<AnalyticsStatEntity, String> {

    List<AnalyticsStatEntity> findByCategory(String category);

    List<AnalyticsStatEntity> findByCategoryOrderByCountDesc(String category);

    @Query("SELECT a FROM AnalyticsStatEntity a WHERE a.category = :category ORDER BY a.count DESC")
    List<AnalyticsStatEntity> findTopByCategory(@Param("category") String category);

    @Modifying
    @Query("UPDATE AnalyticsStatEntity a SET a.count = a.count + 1, a.lastUpdated = CURRENT_TIMESTAMP WHERE a.id = :id")
    int incrementCount(@Param("id") String id);

    @Modifying
    @Query(value = """
        INSERT INTO analytics_stats (id, category, name, count, last_updated)
        VALUES (:id, :category, :name, 1, CURRENT_TIMESTAMP)
        ON CONFLICT (id) DO UPDATE SET count = analytics_stats.count + 1, last_updated = CURRENT_TIMESTAMP
        """, nativeQuery = true)
    void upsertIncrement(@Param("id") String id, @Param("category") String category, @Param("name") String name);
}
