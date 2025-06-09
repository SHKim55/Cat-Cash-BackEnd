package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.entity.UserMissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends JpaRepository<UserMissionEntity, Long> {
    Optional<UserMissionEntity> findByUserMissionId(Long userMissionId);
    
    Optional<UserMissionEntity> findByUserAndExpiredAndCompleted(UserEntity user, Boolean expired, Boolean completed);

    List<UserMissionEntity> findAllByUser(UserEntity user);

    @Override
    <S extends UserMissionEntity> S save(S entity);
}
