package com.my.online_shop.Controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.my.online_shop.Class.Product;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StoreData {

    private static final String KEY_PREFERENCES_NAME = "OnlineShopAppPreferences";

    private Context context;

    public StoreData(Context context) {
        this.context = context;
    }

    public void addToCard(Product product)
    {
        SharedPreferences pref = context.getSharedPreferences(KEY_PREFERENCES_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        List<Product> productList;
        String string = pref.getString("MyOrder", null);
        Type type = new TypeToken<List<Product>>() {
        }.getType();
        productList = gson.fromJson(string, type);
        if(productList==null)
            productList = new ArrayList<>();
        productList.add(product);
        String json = gson.toJson(productList);
        editor.putString("MyOrder", json);
        editor.commit();
    }

    public void updateCardProduct(List<Product> products)
    {
        SharedPreferences pref = context.getSharedPreferences(KEY_PREFERENCES_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(products);
        editor.putString("MyOrder", json);
        editor.commit();
    }

    public void deleteFromCard(int product)
    {
        SharedPreferences pref = context.getSharedPreferences(KEY_PREFERENCES_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        List<Product> productList;
        String string = pref.getString("MyOrder", null);
        Type type = new TypeToken<List<Product>>() {
        }.getType();
        productList = gson.fromJson(string, type);
        Log.d("Lan", Integer.toString(productList.size()));
        productList.remove(product);
        Log.d("Lan", Integer.toString(productList.size()));
        String json = gson.toJson(productList);
        editor.putString("MyOrder", json);
        editor.commit();
    }

    public List<Product> getFromCard()
    {
        SharedPreferences pref = context.getSharedPreferences(KEY_PREFERENCES_NAME, context.MODE_PRIVATE);
        Gson gson = new Gson();
        List<Product> productList;
        String string = pref.getString("MyOrder", null);
        Type type = new TypeToken<List<Product>>() {
        }.getType();
        productList = gson.fromJson(string, type);
        return productList;
    }

}
