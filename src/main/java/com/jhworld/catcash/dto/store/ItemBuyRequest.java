package com.jhworld.catcash.dto.store;

public class ItemBuyRequest {
    private Long itemId;
    private int aftMoney;

    // 기본 생성자
    public ItemBuyRequest() {
    }

    // 모든 필드를 포함한 생성자
    public ItemBuyRequest(Long itemId, int aftMoney) {
        this.itemId = itemId;
        this.aftMoney = aftMoney;
    }

    // Getter & Setter
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getAftMoney() {
        return aftMoney;
    }

    public void setAftMoney(int aftMoney) {
        this.aftMoney = aftMoney;
    }
}