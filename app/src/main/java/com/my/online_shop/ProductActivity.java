package com.my.online_shop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.online_shop.Adapter.CategoryAdapter;
import com.my.online_shop.Adapter.ProductAdapter;
import com.my.online_shop.Class.Category;
import com.my.online_shop.Class.Product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends AppCompatActivity {

    boolean IsAdmin;
    RecyclerView recyclerView;
    Button buttonAddProduct, OrderNow;
    private DatabaseReference mDatabase ;
    private ProductAdapter adapter;
    private List<Product> productList;

    FirebaseUser curr_user;
    String CategoryId, CategoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        recyclerView = findViewById(R.id.recyclerViewProduct);
        buttonAddProduct = findViewById(R.id.AddProduct);
        OrderNow = findViewById(R.id.OrderProduct);

        curr_user = FirebaseAuth.getInstance().getCurrentUser();
        isAdmin();
        Intent intent = getIntent();
        CategoryId = intent.getStringExtra("Id");
        CategoryName = intent.getStringExtra("Name");
        setTitle(CategoryName);
        final ProgressDialog progressDialog = new ProgressDialog(ProductActivity.this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        mDatabase  = FirebaseDatabase.getInstance().getReference("Category/"+CategoryId+"/Product");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList = new ArrayList<>();
                if(dataSnapshot.exists()) {
                    for (final DataSnapshot product : dataSnapshot.getChildren()) {
                        Log.d("OutputData", product.getValue().toString());
                        Log.d("OutputData", product.getKey().toString());
                        productList.add(new Product(
                                product.getKey().toString(),
                                product.getKey().toString(),
                                CategoryId,
                                Integer.parseInt(product.child("Price").getValue().toString())
                        ));
                    }
                }
                adapter = new ProductAdapter(ProductActivity.this,productList, CategoryId);
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ProductActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProductActivity.this));

        recyclerView.setAdapter(adapter);

        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IsAdmin){
                    Intent intent = new Intent(ProductActivity.this, AddProductActivity.class);
                    intent.putExtra("Category", CategoryName);
                    startActivity(intent);
                }
            }
        });

        OrderNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });
    }

    public void isAdmin(){
        mDatabase  = FirebaseDatabase.getInstance().getReference("Admin");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (final DataSnapshot user : dataSnapshot.getChildren()) {
                        if(user.getValue().toString().equals(curr_user.getUid())){
                            IsAdmin = true;
                            buttonAddProduct.setVisibility(View.VISIBLE);
                        }
                    }
                    if(IsAdmin == false)
                        buttonAddProduct.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        return true;
    }

}