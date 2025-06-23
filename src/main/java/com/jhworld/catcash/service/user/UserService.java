package com.jhworld.catcash.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhworld.catcash.configuration.JwtUtil;
import com.jhworld.catcash.dto.ResponseObject;
import com.jhworld.catcash.dto.user.UserCatDTO;
import com.jhworld.catcash.dto.user.UserDTO;
import com.jhworld.catcash.dto.user.UserOnboardDTO;
import com.jhworld.catcash.entity.*;
import com.jhworld.catcash.repository.*;
import com.jhworld.catcash.service.login.LoginService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CatRepository catRepository;
    private final UserCatRepository userCatRepository;
    private final CategoryRepository categoryRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final LastLoginRepository lastLoginRepository;
    private final JwtUtil jwtUtil;
    private final LoginService loginService;

    public UserService(final UserRepository userRepository, final CatRepository catRepository,
                       final UserCatRepository userCatRepository, final CategoryRepository categoryRepository,
                       final UserCategoryRepository userCategoryRepository, LastLoginRepository lastLoginRepository, final JwtUtil jwtUtil, LoginService loginService) {
        this.userRepository = userRepository;
        this.catRepository = catRepository;
        this.userCatRepository = userCatRepository;
        this.categoryRepository = categoryRepository;
        this.userCategoryRepository = userCategoryRepository;
        this.lastLoginRepository = lastLoginRepository;
        this.jwtUtil = jwtUtil;
        this.loginService = loginService;
    }

    public UserEntity findUserByToken(String token) {
        Claims claims = jwtUtil.extractTokenValue(token);
        String userSeq = claims.get("sub", String.class); // userSeq(username) 추출

        Optional<UserEntity> optionalUserEntity = userRepository.findByUserSequence(userSeq);
        return optionalUserEntity.orElse(null);
    }

    @Transactional
    public ResponseEntity<UserCatDTO> onboardUser(String token, UserOnboardDTO userOnboardDTO) {
        System.out.println("onbording...");
        System.out.println(userOnboardDTO);
        UserEntity userEntity = findUserByToken(token);
        if(userEntity == null) {
            System.out.println("Error: User not found");
            return ResponseEntity.status(404).body(null);
        }

        userEntity.setUsername(userOnboardDTO.getUsername());
        userEntity.setModifiedTime(LocalDateTime.now());
        userEntity.setIncome(userOnboardDTO.getIncome());
        userEntity.setFixedExpenditure(userOnboardDTO.getFixedExpenditure());
        userEntity.setSavingProportion(userOnboardDTO.getSavingProportion());
        userEntity.setExpenseType(userOnboardDTO.getExpenseType());
        userEntity.setIsNew(false);

        userRepository.save(userEntity);

//        UserEntity newUserEntity = UserEntity.builder()
//                .userId(userEntity.getUserId())
//                .userSequence(userEntity.getUserSequence())
//                .username(userOnboardDTO.getUsername())
//                .email(userEntity.getEmail())
//                .profileImageUrl(userEntity.getProfileImageUrl())
//                .createdTime(userEntity.getCreatedTime())
//                .modifiedTime(LocalDateTime.now())
//                .gender(userEntity.getGender())
//                .income(userOnboardDTO.getIncome())
//                .fixedExpenditure(userOnboardDTO.getFixedExpenditure())
//                .savingProportion(userOnboardDTO.getSavingProportion())
//                .expenseType(userOnboardDTO.getExpenseType())
//                .isNew(userEntity.getIsNew())
//                .coin(userEntity.getCoin())
//                .build();
//        userRepository.save(newUserEntity);

        for(Long index : userOnboardDTO.getCategoryIdList()) {
            CategoryEntity categoryEntity = categoryRepository.findById(index).orElse(null);
            if(categoryEntity == null) {
                System.out.println("Error : category index " + index + "not found");
                return ResponseEntity.status(404).body(null);
            }

            UserCategoryEntity userCategoryEntity = UserCategoryEntity.builder()
                    .user(userEntity)
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
                .user(userEntity)
                .cat(catEntity)
                .exp(0L)
                .build();
        userCatRepository.save(newUserCatEntity);

//        newUserEntity.setIsNew(false);
//        userRepository.save(newUserEntity);

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

    public ResponseEntity<String> getEnteringData(String token) {
        try {
            final UserEntity userEntity = this.loginService.findUserByToken(token);
            if (userEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다");
            }

            final Optional<UserCatEntity> catEntity = this.userCatRepository.findByUser(userEntity);
            if(catEntity.isEmpty()) {
                return ResponseEntity.status(404).body(null);
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("money", userEntity.getCoin());
            map.put("exp", catEntity.get().getExp());

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(map);

            return ResponseEntity.ok(json);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<Object> isFirstVisitToday(String token) {
        Map<String, Boolean> map = new HashMap<>();
        try {
            final UserEntity userEntity = this.loginService.findUserByToken(token);
            if (userEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다");
            }


            Optional<LastLoginEntity> lastLoginEntity = lastLoginRepository.findByUser(userEntity);
            if(lastLoginEntity.isEmpty()) {
                map.put("isFirstVisitToday", true);
                this.lastLoginRepository.save(LastLoginEntity.builder().user(userEntity).createdTime(LocalDateTime.now()).build());

                Long coin = userEntity.getCoin();
                if (coin == null) {
                    coin = 0L;
                }
                userEntity.setCoin(coin + 500);
                this.userRepository.save(userEntity);
            }
            else {
                ZoneId seoulZone = ZoneId.of("Asia/Seoul");
                ZonedDateTime nowSeoul = ZonedDateTime.now(seoulZone);

                LocalDateTime lastLoginTime = lastLoginEntity.get().getCreatedTime();

                ZonedDateTime lastLoginSeoul = lastLoginTime.atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(seoulZone);

                LocalDate lastLoginDate = lastLoginSeoul.toLocalDate();
                LocalDate todayDate = nowSeoul.toLocalDate();

                if (!lastLoginDate.equals(todayDate)) {
                    map.put("isFirstVisitToday", true);

                    LastLoginEntity newLastLogin = lastLoginEntity.get();

                    // 기록을 갱신
                    newLastLogin.setCreatedTime(LocalDateTime.now());
                    lastLoginRepository.save(newLastLogin);

                    Long coin = userEntity.getCoin();
                    if (coin == null) {
                        coin = 0L;
                    }
                    userEntity.setCoin(coin + 500);
                    this.userRepository.save(userEntity);
                } else {
                    map.put("isFirstVisitToday", false);
                }
            }
            System.out.println(map.get("isFirstVisitToday"));
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<Map<String, String >> getToken() {
        Optional<UserEntity> userEntity = this.userRepository.findById(1L);
        Map<String, String> map = new HashMap<>();
        map.put("body", jwtUtil.generateToken(userEntity.get().getUserSequence()));
        return ResponseEntity.ok(map);
    }
}
