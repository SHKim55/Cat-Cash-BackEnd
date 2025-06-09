package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.LastLoginEntity;
import com.jhworld.catcash.entity.UserCatEntity;
import com.jhworld.catcash.entity.UserEntity;
import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LastLoginRepository extends JpaRepository<LastLoginEntity, Log> {
    Optional<LastLoginEntity> findByUser(UserEntity user);
}
