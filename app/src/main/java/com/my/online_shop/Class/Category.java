package com.my.online_shop.Class;

public class Category {

    String Id, Name;
    int ItemCount;

    public Category(){

    }

    public Category(String id, String name, int itemCount) {
        Id = id;
        Name = name;
        ItemCount = itemCount;
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

    public int getItemCount() {
        return ItemCount;
    }

    public void setItemCount(int itemCount) {
        ItemCount = itemCount;
    }
}
