package com.fincalc.adapter.out.persistence.repository;

import com.fincalc.adapter.out.persistence.entity.LocalizedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalizedMessageRepository extends JpaRepository<LocalizedMessageEntity, String> {
    List<LocalizedMessageEntity> findByCategory(String category);
}
