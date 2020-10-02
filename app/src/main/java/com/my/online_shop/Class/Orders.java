package com.my.online_shop.Class;

import java.util.Comparator;
import java.util.List;

public class Orders {
    List<Product> productList;
    Double Amount;
    String Payment;
    String Deliver, Address, Name, Mobile;
    long timestamp;
    public Orders(List<Product> productList, Double amount, String payment,
                  String deliver, String address, String name, String mobile, long time) {
        this.productList = productList;
        Amount = amount;
        Payment = payment;
        Deliver = deliver;
        Address = address;
        Name = name;
        Mobile = mobile;
        timestamp = time;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public Double getAmount() {
        return Amount;
    }

    public void setAmount(Double amount) {
        Amount = amount;
    }

    public String getPayment() {
        return Payment;
    }

    public void setPayment(String payment) {
        Payment = payment;
    }

    public String getDeliver() {
        return Deliver;
    }

    public void setDeliver(String deliver) {
        Deliver = deliver;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTime() {
        return timestamp;
    }

    public void setTime(long time) {
        timestamp = time;
    }
}
