package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.InventoryEntity;
import com.jhworld.catcash.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    @Override
    Optional<InventoryEntity> findById(Long aLong);

    @Override
    <S extends InventoryEntity> S save(S entity);
}
