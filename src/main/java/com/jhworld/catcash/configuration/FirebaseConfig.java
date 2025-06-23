package com.jhworld.catcash.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Component
public class FirebaseConfig {

    @Value("${fcm.file_path}")
    private String firebaseKeyPath;

    @PostConstruct
    public void initialize() {
        try {
//            FileInputStream serviceAccount = new FileInputStream(firebaseKeyPath);
            ClassPathResource resource = new ClassPathResource("firebase/cat-cash-firebase-sdk.json");


            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
