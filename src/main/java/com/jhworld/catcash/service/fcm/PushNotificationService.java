package com.jhworld.catcash.service.fcm;

import com.google.firebase.messaging.*;
import com.jhworld.catcash.configuration.ChatGptConfig;
import com.jhworld.catcash.configuration.JwtUtil;
import com.jhworld.catcash.dto.llm.GptRequest;
import com.jhworld.catcash.dto.llm.GptResponse;
import com.jhworld.catcash.entity.CategoryEntity;
import com.jhworld.catcash.entity.UserDeviceEntity;
import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.*;
import com.jhworld.catcash.service.llm.GptRole;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PushNotificationService {
    private final UserCategoryRepository userCategoryRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${chatgpt.api.key}")
    private String gptApiKey;

    private static final int randomNumberOfClients = 5;

    public PushNotificationService(final UserCategoryRepository userCategoryRepository,
                                   final UserDeviceRepository userDeviceRepository,
                                   final UserRepository userRepository, final JwtUtil jwtUtil) {
        this.userCategoryRepository = userCategoryRepository;
        this.userDeviceRepository = userDeviceRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    private UserEntity findUserByToken(String token) {
        Claims claims = jwtUtil.extractTokenValue(token);
        String userSeq = claims.get("sub", String.class); // userSeq(username) 추출

        Optional<UserEntity> optionalUserEntity = userRepository.findByUserSequence(userSeq);
        return optionalUserEntity.orElse(null);
    }

    private Notification createNotificationContent(UserEntity userEntity) {
        System.out.println("username: " + userEntity.getUsername());
        CategoryEntity category = userCategoryRepository.findMainCategoryOfUser(userEntity.getUserId());
        PersonalMessageData personalMessageData = PersonalMessageData.builder()
                .username(userEntity.getUsername())
                .mainCategory(category.getCategory())
                .build();

        String gptRequestContent = PushMessageConfig.createMessagePrompt(personalMessageData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        GptRequest request = new GptRequest(
                ChatGptConfig.DEFAULT_MODEL, ChatGptConfig.TEMPERATURE, ChatGptConfig.MAx_TOKENS,
                List.of(new GptRequest.Message(
                        GptRole.system, PushMessageConfig.getSystemPrompt()),
                        new GptRequest.Message(
                        GptRole.user, gptRequestContent)
                ), ChatGptConfig.TOP_P
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<GptRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<GptResponse> response = restTemplate.postForEntity(ChatGptConfig.BASE_URL, entity, GptResponse.class);

        String responseText = response.getBody().getChoices().get(0).getMessage().getContent();

        Map<String, String> responseMap = PushMessageConfig.parseResponseText(responseText);

        Notification notification = Notification.builder()
                .setTitle(responseMap.get("title"))
                .setBody(responseMap.get("content"))
                .build();

        if(notification == null)
            System.out.println("notification 객체 null");

        return notification;
    }

    private List<Message> createClientMessage() {
        List<Message> messages = new ArrayList<>();
        List<UserDeviceEntity> userDeviceEntities = userDeviceRepository.findRandomClients(randomNumberOfClients);

        for(UserDeviceEntity userDevice : userDeviceEntities) {
            try {
                UserEntity userEntity = userRepository.findByUserDevice(userDevice);
                Notification notification = createNotificationContent(userEntity);
                Message message = Message.builder()
                        .setToken(userDevice.getDeviceToken())
                        .setNotification(notification)
                        .build();
                messages.add(message);

                if(message == null)
                    System.out.println("message 객체 null");

            } catch (Exception e) {
                System.out.println("오류 발생 username: " + userDevice.getUser().getUsername());
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        return messages;
    }

//    @Scheduled(fixedRate = 2 * 60 * 1000)   // 10분마다 실행
//    @Scheduled(cron = "0 0 10 ? * MON,WED,SAT", zone = "Asia/Seoul")
    public void sendMessageToClients() {
        System.out.println("푸시메시지 전송");
        sendTestPushMessage("cCFoug5RQTWNCcgw4oKVpy%3AAPA91bGEMAoq3_jFVqv12eDofp7duUhHhuy8LU7rvuSBUblXcKvQeY5YhojHPZzMdkjP1kX9iFW8rLXoWmFVnOLitH8B6cZqmBCjiCDI43W2nMcDwSl49d8=");
//        List<Message> messages;
////        List<UserDeviceEntity> userDeviceEntityList = userDeviceRepository.findAll();
//
//        messages = createClientMessage();
//
//        try {
//            for(Message message : messages) {
//                String response = FirebaseMessaging.getInstance().send(message);
//                System.out.println("푸시 알림 전송 응답: " + response);
//            }
//        } catch (FirebaseMessagingException e) {
//            e.printStackTrace();
//        }
    }

    public ResponseEntity<String> sendTestPushMessage(String deviceToken) {
        System.out.println("deviceToken : " + deviceToken);
        deviceToken = "esJ7fd-dQaexY-G6RzpuXp%3AAPA91bG7BlijR6pgwvE-Tldasn3ydBZUtqBgfaUPm2iqHNGDh3phbpYDyNLY-fkIcdngVWa3uLpixFJwtyLKTy1IbEypTpx9QEw7PRvomA2w2Hb2sGxoKlU=";

        String token = deviceToken;
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle("이수민은 봐라")
                        .setBody("당신은 코딩요정의 저주에 걸렸습니다. 의자를 5바퀴 돌면서 나는 빡빡이다를 외치지 않으면 평생 C언어로 코딩을 해야 합니다.")
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Response: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("Success");
    }

    @Transactional
    public ResponseEntity<String> registerClientDevice(final String authToken, final String deviceToken) {
        UserEntity userEntity = findUserByToken(authToken);
        if(userEntity == null) {
            System.out.println("Error: No such user");
            return ResponseEntity.status(404).body("Error: No such user");
        }

        if(deviceToken.isBlank()) {
            System.out.println("Error: Device token is empty");
            return ResponseEntity.status(404).body("Error: Device token is empty");
        }

        Optional<UserDeviceEntity> isExist = userDeviceRepository.findByUser(userEntity);
        if(isExist.isEmpty()) {
            UserDeviceEntity userDeviceEntity = UserDeviceEntity.builder()
                    .deviceToken(deviceToken)
                    .user(userEntity)
                    .build();
            userDeviceEntity = userDeviceRepository.save(userDeviceEntity);
        }
        else {
            UserDeviceEntity userDeviceEntity = isExist.get();
            userDeviceEntity.setDeviceToken(deviceToken);
            userDeviceRepository.save(userDeviceEntity);
        }

        return ResponseEntity.ok("Success");
    }
}
