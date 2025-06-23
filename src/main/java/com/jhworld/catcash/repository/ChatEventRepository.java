package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.ChatEventEntity;
import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatEventRepository extends JpaRepository<ChatEventEntity, Long> {
    List<ChatEventEntity> findAllByUser(UserEntity user);
}
