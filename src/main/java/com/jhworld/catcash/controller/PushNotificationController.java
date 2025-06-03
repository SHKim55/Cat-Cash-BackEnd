package com.jhworld.catcash.controller;

import com.jhworld.catcash.dto.fcm.PushRegisterDTO;
import com.jhworld.catcash.service.fcm.PushNotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/push")
@AllArgsConstructor
public class PushNotificationController {
    private PushNotificationService pushNotificationService;

    @PostMapping("/test")
    public ResponseEntity<String> sendPushMessageToClient(@RequestBody String deviceToken) {
        System.out.println("Testing push notification with input device token");
        return pushNotificationService.sendTestPushMessage(deviceToken);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerClientDevice(@RequestHeader(name = "Authorization") String authToken,
                                                       @RequestBody String deviceToken) {
        System.out.println("registering device : " + deviceToken);
        return pushNotificationService.registerClientDevice(authToken, deviceToken);
    }
}
