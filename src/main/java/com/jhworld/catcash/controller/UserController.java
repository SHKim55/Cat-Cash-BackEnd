package com.jhworld.catcash.controller;

import com.jhworld.catcash.configuration.JwtUtil;
import com.jhworld.catcash.dto.ResponseObject;
import com.jhworld.catcash.dto.user.UserCatDTO;
import com.jhworld.catcash.dto.user.UserDTO;
import com.jhworld.catcash.dto.user.UserOnboardDTO;
import com.jhworld.catcash.repository.UserRepository;
import com.jhworld.catcash.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/onboard")
    public ResponseEntity<UserCatDTO> onboardUser(@RequestHeader("Authorization") String token, @RequestBody UserOnboardDTO userOnboardDTO) {
        return userService.onboardUser(token, userOnboardDTO);
    }

    @GetMapping("/info")
    public ResponseEntity<UserDTO> getUserInfo(@RequestHeader("Authorization") String token) {
        return userService.getUserInfo(token);
    }

    @GetMapping("/enter/datas")
    public ResponseEntity<String> getEnteringData(@RequestHeader("Authorization") String token) {
        return userService.getEnteringData(token);
    }

    @GetMapping("/first-visit-today")
    public ResponseEntity<Object> isFirstVisitToday(@RequestHeader("Authorization") String token) {
        System.out.println("일일 출석 체크 성공");
        return userService.isFirstVisitToday(token);
    }

    @GetMapping("/getToken")
    public ResponseEntity<Map<String, String>> getToken() {
        return userService.getToken();
    }
}
