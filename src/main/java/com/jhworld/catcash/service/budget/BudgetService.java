package com.jhworld.catcash.service.budget;

import com.jhworld.catcash.dto.budget.BudgetDTO;
import com.jhworld.catcash.dto.budget.BudgetList;
import com.jhworld.catcash.entity.BudgetEntity;
import com.jhworld.catcash.entity.CategoryEntity;
import com.jhworld.catcash.entity.InventoryEntity;
import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.BudgetRepository;
import com.jhworld.catcash.repository.CategoryRepository;
import com.jhworld.catcash.repository.InventoryRepository;
import com.jhworld.catcash.repository.UserRepository;
import com.jhworld.catcash.service.login.LoginService;
import com.jhworld.catcash.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final LoginService loginService;
    private final CategoryRepository categoryRepository;

    public BudgetService(final UserRepository userRepository, final BudgetRepository budgetRepository, final LoginService loginService, final CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
        this.loginService=loginService;
        this.categoryRepository=categoryRepository;
    }

    public ResponseEntity<Boolean> makeNewBudgetData(String token, final BudgetDTO budgetDTO) {
        try {
            final UserEntity userEntity = this.loginService.findUserByToken(token);
            if (userEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다");
            }

            final Optional<CategoryEntity> categoryEntity = this.categoryRepository.findById((long)budgetDTO.categoryId);
            if(categoryEntity.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리가 없습니다");
            }

            final BudgetEntity budget = new BudgetEntity();
            budget.setUser(userEntity);
            budget.setCategory(categoryEntity.get());
            budget.setAmount((long) budgetDTO.amount);
            budget.setCreatedTime(LocalDateTime.now());
            budget.setContent(budgetDTO.content);

            this.budgetRepository.save(budget);

            userEntity.setCoin((long) budgetDTO.aftMoney);
            userRepository.save(userEntity);

            return ResponseEntity.ok(true);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    public ResponseEntity<BudgetList> getAllBudgets(String token) {
        try {
            final UserEntity userEntity = this.loginService.findUserByToken(token);
            if (userEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다");
            }

            final List<BudgetEntity> budgetEntities = this.budgetRepository.findAllByUser(userEntity);

            BudgetDTO[] budgets = new BudgetDTO[budgetEntities.size()];
            int i = 0;
            for (BudgetEntity budget : budgetEntities) {
                BudgetDTO budgetDTO = new BudgetDTO();
                budgetDTO.categoryId = budget.getCategory().getCategoryId().intValue();
                budgetDTO.content = budget.getContent();
                budgetDTO.amount = budget.getAmount().intValue();
                budgetDTO.create_time = budget.getCreatedTime();
                budgets[i++] = budgetDTO;
            }

            BudgetList budgetList = new BudgetList();
            budgetList.budgets = budgets;

            return ResponseEntity.ok(budgetList);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
