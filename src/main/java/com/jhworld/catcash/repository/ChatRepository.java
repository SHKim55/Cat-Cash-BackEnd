package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.ChatEntity;
import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    List<ChatEntity> findAllByUser(UserEntity user);
}
