package com.jhworld.catcash.service.store;

import com.jhworld.catcash.dto.ItemBuyRequest;
import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.InventoryRepository;
import com.jhworld.catcash.repository.ItemRepository;
import com.jhworld.catcash.repository.UserRepository;
import com.jhworld.catcash.service.login.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StoreService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final LoginService loginService;

    public StoreService(final UserRepository userRepository, final ItemRepository itemRepository, final InventoryRepository inventoryRepository, final LoginService loginService) {
        this.userRepository=userRepository;
        this.itemRepository=itemRepository;
        this.inventoryRepository=inventoryRepository;
        this.loginService=loginService;
    }

    public ResponseEntity<Boolean> buyItem(ItemBuyRequest itemBuyRequest, String token) {
        try {
            final UserEntity userEntity = this.loginService.findUserByToken(token);
            if(userEntity==null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다");
            }
        }
    }
}
