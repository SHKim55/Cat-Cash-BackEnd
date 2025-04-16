package com.jhworld.catcash.entity;

import com.jhworld.catcash.service.user.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@Entity
@Table(name = "`user`")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long userId;

    @Column(name = "user_seq", nullable = true)
    private String userSequence;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "profile_image_url", nullable = true)
    private String profileImageUrl;

    @Column(nullable = true)
    private String gender;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;

    @Column(nullable = false)
    @Builder.Default
    private Long income = 0L;

    @Column(name = "fixed_expenditure", nullable = false)
    @Builder.Default
    private Long fixedExpenditure = 0L;

    @Column(name = "saving_proportion", nullable = false)
    @Builder.Default
    private Long savingProportion = 0L;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserCatEntity> userCats;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<InventoryEntity> inventories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BudgetEntity> budgets;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ExpenditureEntity> expenditures;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChatEntity> chats;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChatPromptEntity> chatPrompts;
}
