package com.jhworld.catcash.dto;

public class ItemBuyRequest {
    private String itemId;
    private int aftMoney;

    // 기본 생성자
    public ItemBuyRequest() {
    }

    // 모든 필드를 포함한 생성자
    public ItemBuyRequest(String itemId, int aftMoney) {
        this.itemId = itemId;
        this.aftMoney = aftMoney;
    }

    // Getter & Setter
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getAftMoney() {
        return aftMoney;
    }

    public void setAftMoney(int aftMoney) {
        this.aftMoney = aftMoney;
    }
}