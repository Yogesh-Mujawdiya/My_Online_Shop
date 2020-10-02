package com.my.online_shop.ui;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.online_shop.Adapter.CategoryAdapter;
import com.my.online_shop.AddCategoryActivity;
import com.my.online_shop.Class.Category;
import com.my.online_shop.Controller.StoreData;
import com.my.online_shop.R;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    StoreData controller;
    RecyclerView recyclerView;
    private DatabaseReference mDatabase ;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private Button AddCategory;

    FirebaseUser curr_user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = root.findViewById(R.id.recyclerViewCategory);
        AddCategory = root.findViewById(R.id.AddCategory);
        controller = new StoreData(getContext());
        curr_user = FirebaseAuth.getInstance().getCurrentUser();
        setHasOptionsMenu(true); // Add this!
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        mDatabase  = FirebaseDatabase.getInstance().getReference("Category");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoryList = new ArrayList<>();
                if(dataSnapshot.exists()) {
                    for (final DataSnapshot category : dataSnapshot.getChildren()) {
                            Log.d("OutputData", category.getValue().toString());
                            Log.d("OutputData", category.getKey().toString());
                            categoryList.add(new Category(
                                    category.getKey().toString(),
                                    category.getKey().toString(),
                                    Integer.parseInt(category.child("Count").getValue().toString())
                            ));
                        }
                    }
                adapter = new CategoryAdapter(getContext(),categoryList);
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(adapter);

        AddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(controller.isAdmin()){
                    Intent intent = new Intent(getContext(), AddCategoryActivity.class);
                    startActivity(intent);
                }
            }
        });


        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.home, menu);
        MenuItem item = menu.findItem(R.id.search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);

        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                // Here is where we are going to implement the filter logic
                return true;
            }

        });
    }

}