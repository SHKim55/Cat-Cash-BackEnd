package com.jhworld.catcash.repository;

import com.jhworld.catcash.entity.CategoryEntity;
import com.jhworld.catcash.entity.UserCategoryEntity;
import com.jhworld.catcash.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCategoryRepository extends JpaRepository<UserCategoryEntity, Long> {
    @Override
    Optional<UserCategoryEntity> findById(Long aLong);

    @Override
    List<UserCategoryEntity> findAllById(Iterable<Long> longs);

    List<UserCategoryEntity> findAllByUser(UserEntity user);

    @Query(value = "SELECT e.category FROM expenditure AS e" +
            " WHERE e.user.userId = :userId" +
            " GROUP BY e.category" +
            " ORDER BY COUNT(e) DESC LIMIT 1")
    CategoryEntity findMainCategoryOfUser(@Param("userId") Long userId);

    @Override
    <S extends UserCategoryEntity> S save(S entity);
}
