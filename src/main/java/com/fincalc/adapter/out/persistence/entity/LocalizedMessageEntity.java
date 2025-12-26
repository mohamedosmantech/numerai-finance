package com.fincalc.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "localized_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalizedMessageEntity {

    @Id
    @Column(name = "message_key", length = 100)
    private String messageKey;

    @Column(nullable = false)
    private String category;

    @Column(name = "translations_json", columnDefinition = "TEXT", nullable = false)
    private String translationsJson;

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
