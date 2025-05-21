package com.jhworld.catcash.service.user;

import com.jhworld.catcash.configuration.JwtUtil;
import com.jhworld.catcash.dto.ResponseObject;
import com.jhworld.catcash.dto.user.UserDTO;
import com.jhworld.catcash.dto.user.UserOnboardDTO;
import com.jhworld.catcash.entity.CatEntity;
import com.jhworld.catcash.entity.UserCatEntity;
import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.CatRepository;
import com.jhworld.catcash.repository.UserCatRepository;
import com.jhworld.catcash.repository.UserCategoryRepository;
import com.jhworld.catcash.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CatRepository catRepository;
    private final UserCatRepository userCatRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final JwtUtil jwtUtil;

    public UserService(final UserRepository userRepository, final CatRepository catRepository,
                       final UserCatRepository userCatRepository, final UserCategoryRepository userCategoryRepository,
                       final JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.catRepository = catRepository;
        this.userCatRepository = userCatRepository;
        this.userCategoryRepository = userCategoryRepository;
        this.jwtUtil = jwtUtil;
    }

    public UserEntity findUserByToken(String token) {
        Claims claims = jwtUtil.extractTokenValue(token);
        String userSeq = claims.get("sub", String.class); // userSeq(username) 추출

        Optional<UserEntity> optionalUserEntity = userRepository.findByUserSequence(userSeq);
        return optionalUserEntity.orElse(null);
    }

    public ResponseEntity<ResponseObject> onboardUser(String token, UserOnboardDTO userOnboardDTO) {
        UserEntity userEntity = findUserByToken(token);
        if(userEntity == null) {
            System.out.println("Error: User not found");
            return ResponseEntity.status(404).body(new ResponseObject(404, "Error : User not found", null));
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
                .build();
        userRepository.save(newUserEntity);

//        for(Long index : userOnboardDTO.getCategoryIdList()) {
//
//        }

        Optional<CatEntity> optionalCatEntity = catRepository.findById(1L);  // 고양이 하나만 처리
        if(optionalCatEntity.isEmpty()) {
            System.out.println("Error: Cat not found");
            return ResponseEntity.status(404).body(new ResponseObject(404, "Error : User not found", null));
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

        return ResponseEntity.ok(new ResponseObject(200, "Success", catEntity));
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
