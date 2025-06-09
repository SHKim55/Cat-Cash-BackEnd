package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.ExpenditureEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpenditureRepository extends JpaRepository<ExpenditureEntity, Long> {
    Optional<ExpenditureEntity> findByExpenditureId(Long expenditureId);

    @Override
    <S extends ExpenditureEntity> S save(S entity);
}
