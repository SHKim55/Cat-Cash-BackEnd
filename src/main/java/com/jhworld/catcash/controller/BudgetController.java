package com.jhworld.catcash.controller;

import com.jhworld.catcash.dto.budget.BudgetDTO;
import com.jhworld.catcash.dto.budget.BudgetList;
import com.jhworld.catcash.service.budget.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/budget")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(final BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/new")
    public ResponseEntity<Boolean> makeNewBudgetData(@RequestHeader("Authorization") String token, @RequestBody() BudgetDTO budgetDTO) {
        return budgetService.makeNewBudgetData(token, budgetDTO);
    }

    @GetMapping("/all")
    public ResponseEntity<BudgetList> getAllBudgets(@RequestHeader("Authorization") String token) {
        return budgetService.getAllBudgets(token);
    }
}
