package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.ExpenditureEntity;

import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenditureRepository extends JpaRepository<ExpenditureEntity, Long> {
    Optional<ExpenditureEntity> findByExpenditureId(Long expenditureId);

    List<ExpenditureEntity> findAllByUser(UserEntity user);

    @Override
    <S extends ExpenditureEntity> S save(S entity);
}
