package com.my.online_shop.Class;

import java.io.Serializable;

public class Product implements Serializable {

    String Id, Name, Category;
    double Price;
    int Quantity;

    public Product() {
    }

    public Product(String id, String name, String category, double price) {
        Id = id;
        Name = name;
        Category = category;
        Price = price;
        Quantity = 0;
    }

    public Product(String id, String name, String category, double price, int quantity) {
        Id = id;
        Name = name;
        Category = category;
        Price = price;
        Quantity = quantity;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }
}
