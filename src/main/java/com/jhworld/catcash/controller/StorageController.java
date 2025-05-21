package com.jhworld.catcash.controller;

import com.jhworld.catcash.service.storage.StorageService;
import com.jhworld.catcash.service.store.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/storage")
public class StorageController {
    private final StorageService storageService;

    public StorageController(final StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/items")
    public ResponseEntity<String> getUserItems(@RequestHeader("Authorization") String token) {
        return storageService.getUserItems(token);
    }

    @GetMapping("/use/{itemId}")
    public ResponseEntity<Boolean> useItem(
            @PathVariable("itemId") Long itemId,
            @RequestHeader("Authorization") String token
    ) {
        System.out.println("running!");
        return storageService.useItem(itemId, token);
    }
}
