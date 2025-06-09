package com.jhworld.catcash.pg.repository;

import com.jhworld.catcash.entity.pg.VectorChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VectorChatRepository extends JpaRepository<VectorChatEntity, Long> {
    @Override
    <S extends VectorChatEntity> S save(S entity);

    @Query(
            value = """
        SELECT *
        FROM vector_chat vc
        WHERE vc.user_id = :userId
          AND (
              (vc.embedding <=> CAST(:embedding1 AS vector)) < :threshold
           OR (vc.embedding <=> CAST(:embedding2 AS vector)) < :threshold
           OR (vc.embedding <=> CAST(:embedding3 AS vector)) < :threshold
          )
        ORDER BY LEAST(
            vc.embedding <=> CAST(:embedding1 AS vector),
            vc.embedding <=> CAST(:embedding2 AS vector),
            vc.embedding <=> CAST(:embedding3 AS vector)
        )
        LIMIT :limit
      """,
            nativeQuery = true
    )
    List<VectorChatEntity> findNearestByUserAnyOfThreeWithinThreshold(
            @Param("userId")    Long   userId,
            @Param("embedding1") String embedding1,
            @Param("embedding2") String embedding2,
            @Param("embedding3") String embedding3,
            @Param("threshold")  float  threshold,
            @Param("limit")      int    limit
    );
}
