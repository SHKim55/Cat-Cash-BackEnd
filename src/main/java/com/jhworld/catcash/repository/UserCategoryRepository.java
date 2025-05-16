package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.UserCategoryEntity;
import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCategoryRepository extends JpaRepository<UserCategoryEntity, Long> {
    @Override
    Optional<UserCategoryEntity> findById(Long aLong);

    @Override
    List<UserCategoryEntity> findAllById(Iterable<Long> longs);

    List<UserCategoryEntity> findAllByUser(UserEntity user);

    @Override
    <S extends UserCategoryEntity> S save(S entity);
}
