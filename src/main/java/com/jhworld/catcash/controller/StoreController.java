package com.jhworld.catcash.controller;

import com.jhworld.catcash.dto.ItemBuyRequest;
import com.jhworld.catcash.service.store.StoreService;
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
    public ResponseEntity<String> buyItem(@RequestBody ItemBuyRequest itemBuyRequest) {
        return storeService.buyItem(itemBuyRequest);
    }
}
