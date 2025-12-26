package com.fincalc.adapter.out.persistence.repository;

import com.fincalc.adapter.out.persistence.entity.LegalPageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalPageRepository extends JpaRepository<LegalPageEntity, String> {
}
