package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.CatEntity;
import com.jhworld.catcash.entity.LastLoginEntity;
import com.jhworld.catcash.entity.RecentChatEntity;
import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecentChatRepository extends JpaRepository<RecentChatEntity, Long> {
    Optional<RecentChatEntity> findByUser(UserEntity user);
}
