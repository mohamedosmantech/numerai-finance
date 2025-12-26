package com.fincalc.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "analytics_daily",
       uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "category", "name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDailyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "count")
    private Long count = 0L;

    public AnalyticsDailyEntity(LocalDate date, String category, String name) {
        this.statDate = date;
        this.category = category;
        this.name = name;
        this.count = 0L;
    }
}
