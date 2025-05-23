package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.BudgetEntity;
import com.jhworld.catcash.entity.CategoryEntity;
import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {
    @Override
    <S extends BudgetEntity> S save(S entity);

    List<BudgetEntity> findAllByUser(UserEntity user);

    @Override
    Optional<BudgetEntity> findById(Long aLong);
}
