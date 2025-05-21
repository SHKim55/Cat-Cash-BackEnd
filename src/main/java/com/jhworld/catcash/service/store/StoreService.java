package com.jhworld.catcash.service.store;

import com.jhworld.catcash.dto.store.ItemBuyRequest;
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

import java.util.Optional;

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
            if (userEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다");
            }

            // 코인 업데이트
            userEntity.setCoin((long) itemBuyRequest.getAftMoney());

            // 아이템 확인
            final Optional<ItemEntity> item = this.itemRepository.findById(itemBuyRequest.getItemId());
            if (item.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 아이템이 서버에 없습니다");
            }
            final ItemEntity checkedItem = item.get();

            // 인벤토리 저장
            final InventoryEntity inventoryEntity = new InventoryEntity();
            inventoryEntity.setUser(userEntity);
            inventoryEntity.setItem(checkedItem);
            this.inventoryRepository.save(inventoryEntity);

            // 유저 상태 저장 (코인 업데이트 적용)
            this.userRepository.save(userEntity);

            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace(); // 디버깅용, 운영 환경에서는 로깅 처리 권장
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
