package com.jhworld.catcash.service.fcm;

import com.google.firebase.messaging.*;
import com.jhworld.catcash.dto.fcm.PushRegisterDTO;
import com.jhworld.catcash.entity.UserDeviceEntity;
import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.UserDeviceRepository;
import com.jhworld.catcash.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PushNotificationService {
    private UserDeviceRepository userDeviceRepository;
    private UserRepository userRepository;

//    @Scheduled(fixedRate = 10 * 60 * 1000)   // 10분마다 실행
//    @Scheduled(cron = "0 0 10 ? * MON,WED,SAT", zone = "Asia/Seoul")
    public void sendMessageToClients() {
        List<UserDeviceEntity> userDeviceEntityList = userDeviceRepository.findAll();

        List<Message> messages = userDeviceEntityList.stream()
                .map(entity -> Message.builder()
                        .setToken(entity.getDeviceToken())
                        .setNotification(Notification.builder()
                                .setTitle("타이틀")
                                .setBody("푸시메시지 본문")
                                .build()
                        ).build()
                ).toList();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendAll(messages);
            System.out.printf("%d Attempts, %d Success, %d Failure\n",
                    response.getResponses().size(),
                    response.getSuccessCount(),
                    response.getFailureCount()
            );
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    public ResponseEntity<String> sendTestPushMessage(String deviceToken) {
        Optional<UserDeviceEntity> optionalUserDeviceEntity = userDeviceRepository.findByDeviceToken(deviceToken);
        if(optionalUserDeviceEntity.isEmpty()) {
            System.out.println("Error: Invalid device Token");
            return ResponseEntity.status(404).body("Error: Invalid device Token");
        }

        String token = optionalUserDeviceEntity.get().getDeviceToken();
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle("테스트 메시지")
                        .setBody("테스트 메시지 본문")
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
    public ResponseEntity<String> registerClientDevice(final PushRegisterDTO pushRegisterDTO) {
        if(pushRegisterDTO == null) {
            System.out.println("Error: Empty request body for push alarm registration");
            return ResponseEntity.status(404).body("Error: Empty request body for push alarm registration");
        }

        Optional<UserEntity> optionalUserEntity = userRepository.findById(pushRegisterDTO.getUserId());
        if(optionalUserEntity.isEmpty()) {
            System.out.println("Error: Invalid user id");
            return ResponseEntity.status(404).body("Error: Invalid user id");
        }

        UserDeviceEntity userDeviceEntity = UserDeviceEntity.builder()
                .deviceToken(pushRegisterDTO.getDeviceToken())
                .user(optionalUserEntity.get())
                .build();
        userDeviceEntity = userDeviceRepository.save(userDeviceEntity);

        return ResponseEntity.ok("Success");
    }
}
