package com.jhworld.catcash.service.user;

import com.jhworld.catcash.configuration.JwtUtil;
import com.jhworld.catcash.dto.ResponseObject;
import com.jhworld.catcash.dto.user.UserCatDTO;
import com.jhworld.catcash.dto.user.UserDTO;
import com.jhworld.catcash.dto.user.UserOnboardDTO;
import com.jhworld.catcash.entity.*;
import com.jhworld.catcash.repository.*;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CatRepository catRepository;
    private final UserCatRepository userCatRepository;
    private final CategoryRepository categoryRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final JwtUtil jwtUtil;

    public UserService(final UserRepository userRepository, final CatRepository catRepository,
                       final UserCatRepository userCatRepository, final CategoryRepository categoryRepository,
                       final UserCategoryRepository userCategoryRepository, final JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.catRepository = catRepository;
        this.userCatRepository = userCatRepository;
        this.categoryRepository = categoryRepository;
        this.userCategoryRepository = userCategoryRepository;
        this.jwtUtil = jwtUtil;
    }

    public UserEntity findUserByToken(String token) {
        Claims claims = jwtUtil.extractTokenValue(token);
        String userSeq = claims.get("sub", String.class); // userSeq(username) 추출

        Optional<UserEntity> optionalUserEntity = userRepository.findByUserSequence(userSeq);
        return optionalUserEntity.orElse(null);
    }

    @Transactional
    public ResponseEntity<UserCatDTO> onboardUser(String token, UserOnboardDTO userOnboardDTO) {
        UserEntity userEntity = findUserByToken(token);
        if(userEntity == null) {
            System.out.println("Error: User not found");
            return ResponseEntity.status(404).body(null);
        }

        UserEntity newUserEntity = UserEntity.builder()
                .userId(userEntity.getUserId())
                .userSequence(userEntity.getUserSequence())
                .username(userOnboardDTO.getUsername())
                .email(userEntity.getEmail())
                .profileImageUrl(userEntity.getProfileImageUrl())
                .createdTime(userEntity.getCreatedTime())
                .modifiedTime(LocalDateTime.now())
                .gender(userEntity.getGender())
                .income(userOnboardDTO.getIncome())
                .fixedExpenditure(userOnboardDTO.getFixedExpenditure())
                .savingProportion(userOnboardDTO.getSavingProportion())
                .expenseType(userOnboardDTO.getExpenseType())
                .isNew(userEntity.getIsNew())
                .coin(userEntity.getCoin())
                .build();
        userRepository.save(newUserEntity);

        for(Long index : userOnboardDTO.getCategoryIdList()) {
            CategoryEntity categoryEntity = categoryRepository.findById(index).orElse(null);
            if(categoryEntity == null) {
                System.out.println("Error : category index " + index + "not found");
                return ResponseEntity.status(404).body(null);
            }

            UserCategoryEntity userCategoryEntity = UserCategoryEntity.builder()
                    .user(newUserEntity)
                    .category(categoryEntity)
                    .build();

            userCategoryRepository.save(userCategoryEntity);
        }

        Optional<CatEntity> optionalCatEntity = catRepository.findById(1L);  // 고양이 하나만 처리
        if(optionalCatEntity.isEmpty()) {
            System.out.println("Error: Cat not found");
            return ResponseEntity.status(404).body(null);
        }

        CatEntity catEntity = optionalCatEntity.get();
        UserCatEntity newUserCatEntity = UserCatEntity.builder()
                .customName(catEntity.getName())
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .user(newUserEntity)
                .cat(catEntity)
                .exp(0L)
                .build();
        newUserCatEntity = userCatRepository.save(newUserCatEntity);

        newUserEntity.setIsNew(false);
        userRepository.save(newUserEntity);

        UserCatDTO userCatDTO = UserCatDTO.convertEntityToDTO(newUserCatEntity);
        return ResponseEntity.ok(userCatDTO);
    }

    public ResponseEntity<UserDTO> getUserInfo(String token) {
        UserEntity userEntity = findUserByToken(token);
        if(userEntity == null) {
            System.out.println("Error: User not found");
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(UserDTO.convertEntityToDTO(userEntity));
    }
}
