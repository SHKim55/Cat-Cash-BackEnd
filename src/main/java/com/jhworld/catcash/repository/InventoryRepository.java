package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.InventoryEntity;
import com.jhworld.catcash.entity.ItemEntity;
import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    @Override
    Optional<InventoryEntity> findById(Long aLong);

    List<InventoryEntity> findAllByUser(UserEntity user);

    Optional<InventoryEntity> findFirstByUserAndItem(UserEntity user, ItemEntity item);

    @Override
    <S extends InventoryEntity> S save(S entity);
}
