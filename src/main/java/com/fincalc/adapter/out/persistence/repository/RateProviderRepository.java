package com.fincalc.adapter.out.persistence.repository;

import com.fincalc.adapter.out.persistence.entity.RateProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RateProviderRepository extends JpaRepository<RateProviderEntity, String> {
    List<RateProviderEntity> findByEnabledTrue();
}
