package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    @Override
    <S extends CategoryEntity> S save(S entity);

    @Override
    Optional<CategoryEntity> findById(Long aLong);
}
