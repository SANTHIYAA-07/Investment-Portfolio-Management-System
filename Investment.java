package org.example;

public class Investment {

    private int investmentId;
    private int userId;
    private String assetName;
    private String assetType;
    private double quantity;
    private double purchasePrice;
    private double currentPrice;
    private String purchaseDate;

    public Investment(
            int userId,
            String assetName,
            String assetType,
            double quantity,
            double purchasePrice,
            double currentPrice,
            String purchaseDate) {

        this.userId = userId;
        this.assetName = assetName;
        this.assetType = assetType;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
        this.purchaseDate = purchaseDate;
    }

    public int getUserId() {
        return userId;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }
}