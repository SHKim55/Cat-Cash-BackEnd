package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.CatEntity;
import com.jhworld.catcash.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
    @Override
    Optional<ItemEntity> findById(Long aLong);

    @Override
    <S extends ItemEntity> S save(S entity);
}
