package com.jhworld.catcash.controller;

import com.jhworld.catcash.dto.store.ItemBuyRequest;
import com.jhworld.catcash.service.store.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;

    public StoreController(final StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/buy")
    public ResponseEntity<Boolean> buyItem(@RequestBody ItemBuyRequest itemBuyRequest, @RequestHeader("Authorization") String token) {
        System.out.println("hello");
        return storeService.buyItem(itemBuyRequest, token);
    }
}
