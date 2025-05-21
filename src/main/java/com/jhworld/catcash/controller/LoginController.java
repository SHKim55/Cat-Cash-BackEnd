package com.jhworld.catcash.controller;

import com.jhworld.catcash.dto.login.NewUserCheckDTO;
import com.jhworld.catcash.service.login.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {
    private final LoginService loginService;

    public LoginController(final LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/page/google")
    public ResponseEntity<String> loadGoogleLoginPage() {
        return loginService.loadGoogleLoginPage();
    }

    @GetMapping("/process/google")
    public void logInViaGoogle(@RequestParam(name = "code") String code) {
        loginService.logInViaGoogle(code);
    }

    @GetMapping("/page/kakao")
    public ResponseEntity<String> loadKakaoLoginPage() {
//        return loginService.loadKakaoLoginPage();
        return null;
    }

    @GetMapping("/process/kakao")
    public String logInViaKakao() {
        return null;
    }

    @PostMapping("/new")
    public ResponseEntity<NewUserCheckDTO> isNewUser(@RequestHeader(name = "Authorization") String token) {
        return loginService.isNewUser(token);
    }
}
