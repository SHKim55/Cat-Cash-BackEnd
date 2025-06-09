package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.UserDeviceEntity;
import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDeviceEntity, Long> {
    @Override
    Optional<UserDeviceEntity> findById(Long aLong);

    Optional<UserDeviceEntity> findByUser(UserEntity user);
    
    Optional<UserDeviceEntity> findByDeviceToken(String deviceToken);

    @Query(value = "SELECT * FROM user_device ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<UserDeviceEntity> findRandomClients(@Param("limit") int limit);

    @Override
    <S extends UserDeviceEntity> List<S> findAll(Example<S> example);

    @Override
    <S extends UserDeviceEntity> S save(S entity);
}
