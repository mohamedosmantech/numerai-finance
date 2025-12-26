package com.fincalc.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsStatEntity {

    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "count")
    private Long count = 0L;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }

    public AnalyticsStatEntity(String category, String name) {
        this.id = category + ":" + name;
        this.category = category;
        this.name = name;
        this.count = 0L;
    }
}
