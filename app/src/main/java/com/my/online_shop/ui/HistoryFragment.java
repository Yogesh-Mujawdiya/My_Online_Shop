package com.my.online_shop.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
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
import com.my.online_shop.Adapter.MyOrderAdapter;
import com.my.online_shop.Class.Category;
import com.my.online_shop.Class.Order;
import com.my.online_shop.Class.Product;
import com.my.online_shop.R;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private TextView textViewDelivered, textViewPending, textViewCanceled;
    private RecyclerView recyclerView;
    private DatabaseReference mDatabase ;
    private MyOrderAdapter adapter;
    private List<Order> ordersList;
    private ProgressDialog progressDialog;

    private boolean IsAdmin;

    private FirebaseUser user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = root.findViewById(R.id.MyOrder);
        textViewDelivered = root.findViewById(R.id.OrderDelivered);
        textViewPending = root.findViewById(R.id.OrderPending);
        textViewCanceled = root.findViewById(R.id.OrderCanceled);


        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        isAdmin();
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(adapter);
        textViewDelivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewDelivered.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.toggle_on));
                textViewPending.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.toggle_off));
                textViewCanceled.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.toggle_off));
                textViewDelivered.setTextColor(getActivity().getResources().getColor(R.color.black));
                textViewPending.setTextColor(getActivity().getResources().getColor(R.color.white));
                textViewCanceled.setTextColor(getActivity().getResources().getColor(R.color.white));
                adapter.getFilter().filter("Delivered");
            }
        });
        textViewPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewDelivered.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.toggle_off));
                textViewPending.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.toggle_on));
                textViewCanceled.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.toggle_off));
                textViewDelivered.setTextColor(getActivity().getResources().getColor(R.color.white));
                textViewPending.setTextColor(getActivity().getResources().getColor(R.color.black));
                textViewCanceled.setTextColor(getActivity().getResources().getColor(R.color.white));
                adapter.getFilter().filter("Pending");
            }
        });
        textViewCanceled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewDelivered.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.toggle_off));
                textViewPending.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.toggle_off));
                textViewCanceled.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.toggle_on));
                textViewDelivered.setTextColor(getActivity().getResources().getColor(R.color.white));
                textViewPending.setTextColor(getActivity().getResources().getColor(R.color.white));
                textViewCanceled.setTextColor(getActivity().getResources().getColor(R.color.black));
                adapter.getFilter().filter("Canceled");
            }
        });

        return root;
    }

    private void getOrder(){
        mDatabase  = FirebaseDatabase.getInstance().getReference("User/"+user.getUid());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                ordersList = new ArrayList<>();
                if(dataSnapshot.exists()) {
                    List<String> orders = new ArrayList<>();
                    for (final DataSnapshot order : dataSnapshot.getChildren()) {
                        orders.add(order.getValue().toString());
                    }
                    getMyOrder(orders);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void getMyOrder(final List<String> order){

        mDatabase  = FirebaseDatabase.getInstance().getReference("Order");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList = new ArrayList<>();
                for(String id : order){
                    DataSnapshot O = snapshot.child(id);
                    List<Product> productList = new ArrayList<>();
                    for (DataSnapshot product : O.child("productList").getChildren()){
                        productList.add(new Product(
                                product.child("id").getValue().toString(),
                                product.child("name").getValue().toString(),
                                product.child("category").getValue().toString(),
                                Double.parseDouble(product.child("price").getValue().toString()),
                                Integer.parseInt(product.child("quantity").getValue().toString())
                        ));
                    }
                    ordersList.add(new Order(
                            id,
                            productList,
                            Double.parseDouble(O.child("amount").getValue().toString()),
                            O.child("payment").getValue().toString(),
                            O.child("deliver").getValue().toString(),
                            O.child("address").getValue().toString(),
                            O.child("name").getValue().toString(),
                            O.child("mobile").getValue().toString(),
                            Long.parseLong(O.child("time").getValue().toString())
                    ));
                }
                Collections.sort(ordersList,Order.OrderTime);
                adapter = new MyOrderAdapter(getContext(),ordersList,IsAdmin);
                adapter.getFilter().filter("Pending");
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAllOrder(){

        mDatabase  = FirebaseDatabase.getInstance().getReference("Order");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList = new ArrayList<>();
                for(DataSnapshot O : snapshot.getChildren()){
                    List<Product> productList = new ArrayList<>();
                    for (DataSnapshot product : O.child("productList").getChildren()){
                        productList.add(new Product(
                                product.child("id").getValue().toString(),
                                product.child("name").getValue().toString(),
                                product.child("category").getValue().toString(),
                                Double.parseDouble(product.child("price").getValue().toString()),
                                Integer.parseInt(product.child("quantity").getValue().toString())
                        ));
                    }
                    ordersList.add(new Order(
                            O.getKey(),
                            productList,
                            Double.parseDouble(O.child("amount").getValue().toString()),
                            O.child("payment").getValue().toString(),
                            O.child("deliver").getValue().toString(),
                            O.child("address").getValue().toString(),
                            O.child("name").getValue().toString(),
                            O.child("mobile").getValue().toString(),
                            Long.parseLong(O.child("time").getValue().toString())
                    ));
                }
                Collections.sort(ordersList,Order.OrderTime);
                adapter = new MyOrderAdapter(getContext(),ordersList, IsAdmin);
                adapter.getFilter().filter("Pending");
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void isAdmin(){
        mDatabase  = FirebaseDatabase.getInstance().getReference("Admin");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (final DataSnapshot data : dataSnapshot.getChildren()) {
                        if(data.getValue().toString().equals(user.getUid())){
                            IsAdmin = true;
                            break;
                        }
                    }
                    if(IsAdmin)
                        getOrder();
                    else
                        getAllOrder();

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}