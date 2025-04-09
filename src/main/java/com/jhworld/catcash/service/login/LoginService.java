package com.jhworld.catcash.service.login;

import com.jhworld.catcash.configuration.GoogleLoginConfig;
import com.jhworld.catcash.configuration.JwtUtil;
import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final HttpServletResponse httpServletResponse;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Autowired
    private GoogleLoginConfig googleLoginConfig;

    @Value("${client.redirect.uri}")
    private String clientRedirectUri;

    public LoginService(final UserRepository userRepository, final AuthenticationManager authenticationManager,
                        final HttpServletResponse httpServletResponse, final JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.httpServletResponse = httpServletResponse;
        this.jwtUtil = jwtUtil;
        this.restTemplate = new RestTemplate();
    }

    private String requestGoogleAccessToken(final String code) {
        final String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        GoogleLoginRequest googleClient = GoogleLoginRequest.builder().code(decodedCode).build();
        HttpEntity<GoogleLoginRequest> httpEntity = new HttpEntity<>(googleClient, headers);

        // 구글 Access Token 요청
        final GoogleLoginResponse response = restTemplate.exchange(
                googleLoginConfig.getAccessTokenUri(), HttpMethod.POST, httpEntity, GoogleLoginResponse.class
        ).getBody();

        return response.getAccessToken();
    }

    private GoogleUserProfile requestGoogleUserProfile(final String accessToken) {
        // Access Token을 통해 사용자 정보 휙득
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        final HttpEntity<GoogleLoginRequest> httpEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(googleLoginConfig.getUserProfileUri(), HttpMethod.GET, httpEntity, GoogleUserProfile.class)
                .getBody();
    }

    public ResponseEntity<String> loadGoogleLoginPage() {
        String uri = googleLoginConfig.getLoginPageUrl() + "?"
                + "client_id=" + googleLoginConfig.getClientId()
                + "&redirect_uri=" + googleLoginConfig.getRedirectUri()
                + "&response_type=" + googleLoginConfig.getResponseType()
                + "&scope=" + googleLoginConfig.getScope();

        System.out.println(uri);
        return ResponseEntity.ok(uri);
    }

    @Transactional
    public void logInViaGoogle(String code) {
        UserEntity userEntity;
        GoogleUserProfile googleUserProfile;
        String jwtToken;

        try {
            String accessToken = requestGoogleAccessToken(code);
            googleUserProfile = requestGoogleUserProfile(accessToken);
        } catch (Exception e) {
            System.out.println("Error: failed to get user profile from google\n" + e.getMessage());
            return;
        }

        try {
            Optional<UserEntity> optionalUserEntity = userRepository.findByUserSequence(googleUserProfile.getId());
            if (optionalUserEntity.isEmpty()) {   // 회원가입
                UserEntity newUserEntity = UserEntity.builder()
                        .userSequence(googleUserProfile.getId())
                        .email(googleUserProfile.getEmail())
                        .username(googleUserProfile.getName())
                        .gender(googleUserProfile.getGender())
                        .profileImageUrl(googleUserProfile.getPicture())
                        .createdTime(LocalDateTime.now())
                        .modifiedTime(LocalDateTime.now())
                        .build();

                userEntity = userRepository.save(newUserEntity);
            } else {
                userEntity = optionalUserEntity.get();
            }
        } catch (Exception e) {
            System.out.println("Error: failed to load or save user profile\n");
            return;
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    // UserSequence 기반
                    new UsernamePasswordAuthenticationToken(userEntity.getUserSequence(), null)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            jwtToken = jwtUtil.generateToken(userDetails.getUsername());


        } catch (AuthenticationException e) {
            e.printStackTrace();
            System.out.println("Error: failed to create jwt token");
            return;
        }

        try {
            String redirectUrl = UriComponentsBuilder.fromUriString(clientRedirectUri)
                    .queryParam("token", jwtToken)
                    .build().toUriString();
            httpServletResponse.sendRedirect(redirectUrl);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: failed to redirect to client");
        }
    }
}
