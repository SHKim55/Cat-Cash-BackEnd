package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.UserCatEntity;
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCatRepository extends JpaRepository<UserCatEntity, Log> {
    @Override
    Optional<UserCatEntity> findById(Log log);

    @Override
    <S extends UserCatEntity> S save(S entity);
}
