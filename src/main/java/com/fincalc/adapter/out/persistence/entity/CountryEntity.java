package com.fincalc.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "countries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryEntity {

    @Id
    @Column(length = 2)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "tax_system_json", columnDefinition = "TEXT")
    private String taxSystemJson;

    @Column(name = "regions_json", columnDefinition = "TEXT")
    private String regionsJson;

    @Column(name = "has_regional_tax")
    private boolean hasRegionalTax;

    @Column(name = "rate_source")
    private String rateSource;

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
