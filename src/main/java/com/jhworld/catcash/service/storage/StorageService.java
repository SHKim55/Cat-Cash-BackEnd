package com.jhworld.catcash.service.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhworld.catcash.dto.storage.InventoryItemResponseDto;
import com.jhworld.catcash.entity.InventoryEntity;
import com.jhworld.catcash.entity.ItemEntity;
import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.InventoryRepository;
import com.jhworld.catcash.repository.ItemRepository;
import com.jhworld.catcash.repository.UserRepository;
import com.jhworld.catcash.service.login.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StorageService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final LoginService loginService;

    public StorageService(final UserRepository userRepository, final ItemRepository itemRepository, final InventoryRepository inventoryRepository, final LoginService loginService) {
        this.userRepository=userRepository;
        this.itemRepository=itemRepository;
        this.inventoryRepository=inventoryRepository;
        this.loginService=loginService;
    }

    public ResponseEntity<String> getUserItems(String token) {
        try {
            final UserEntity userEntity = this.loginService.findUserByToken(token);
            if (userEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다");
            }

            final List<InventoryEntity> allItems = this.inventoryRepository.findAllByUser(userEntity);

            HashMap<Long, Integer> map = new HashMap<>();

            for(final InventoryEntity item: allItems) {
                map.put(item.getItem().getItemId(), map.getOrDefault(item.getItem().getItemId(), 0) + 1);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(map);

            return ResponseEntity.ok(json);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("false");
        }
    }

    public ResponseEntity<Boolean> useItem(Long itemId, String token) {
        try {
            final UserEntity userEntity = this.loginService.findUserByToken(token);
            if (userEntity == null) {
                System.out.println("해당 유저가 존재하지 않습니다");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다");
            }

            final Optional<ItemEntity> item = this.itemRepository.findById(itemId);
            if(item.isEmpty()) {
                System.out.println("해당 아이템이 존재하지 않습니다");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 아이템이 존재하지 않습니다");
            }

            final Optional<InventoryEntity> ownedItem = this.inventoryRepository.findFirstByUserAndItem(userEntity, item.get());

            if (ownedItem.isEmpty()) {
                System.out.println("해당 아이템을 보유하고 있지 않습니다");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 아이템을 보유하고 있지 않습니다");
            }

            // 삭제
            this.inventoryRepository.delete(ownedItem.get());

            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }


}
