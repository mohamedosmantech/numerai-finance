package com.fincalc.adapter.out.persistence.repository;

import com.fincalc.adapter.out.persistence.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyEntity, String> {
}
