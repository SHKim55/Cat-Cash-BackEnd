package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Override
    Optional<UserEntity> findById(Long aLong);

    Optional<UserEntity> findByUserSequence(String userSequence);

    @Override
    <S extends UserEntity> S save(S entity);
}
