package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_device")
public class UserDeviceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_device_id", nullable = false)
    private Long userDeviceId;

    @Column(name = "device_token", nullable = false)
    private String deviceToken;


    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
