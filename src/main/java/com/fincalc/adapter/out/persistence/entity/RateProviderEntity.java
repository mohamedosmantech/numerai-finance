package com.fincalc.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rate_providers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateProviderEntity {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "api_key_env_var")
    private String apiKeyEnvVar;

    @Column(name = "series_mapping_json", columnDefinition = "TEXT")
    private String seriesMappingJson;

    @Column(name = "cache_duration_minutes")
    private int cacheDurationMinutes;

    private boolean enabled;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
