package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.CatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatRepository extends JpaRepository<CatEntity, Long> {
    @Override
    Optional<CatEntity> findById(Long aLong);

    @Override
    <S extends CatEntity> S save(S entity);
}
