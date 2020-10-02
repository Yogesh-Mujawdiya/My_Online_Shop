package com.my.online_shop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.my.online_shop.Adapter.PlaceOrderAdapter;
import com.my.online_shop.Class.Category;
import com.my.online_shop.Class.Order;
import com.my.online_shop.Class.Orders;
import com.my.online_shop.Class.Product;
import com.my.online_shop.Controller.StoreData;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class OrderActivity extends AppCompatActivity {


    private PlaceOrderAdapter adapter;
    private List<Product> productsList;
    private Button buttonContinue;
    private TextView paymentAmount;
    RecyclerView recyclerView;
    StoreData controller;
    final int UPI_PAYMENT = 0;
    FirebaseUser user;
    String Address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        user = FirebaseAuth.getInstance().getCurrentUser();
        controller = new StoreData(this);
        paymentAmount = findViewById(R.id.TotalAmount);
        buttonContinue = findViewById(R.id.bottomContinueOrder);
        productsList = controller.getFromCard();
        adapter = new PlaceOrderAdapter(OrderActivity.this,productsList);
        recyclerView = findViewById(R.id.MyOrder);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(OrderActivity.this);
        recyclerView.setLayoutManager(layoutManager);


        final Handler handler=new Handler();
        final Runnable updatePrice=new Runnable() {
            @Override
            public void run() {
                paymentAmount.setText(Double.toString(adapter.getPrice()));
                handler.postDelayed(this,100);
            }
        };
        handler.postDelayed(updatePrice,100);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(OrderActivity.this);
                dialog.setContentView(R.layout.dialog_get_address);
                final EditText editTextAddress = dialog.findViewById(R.id.editTextAddress);
                Button buttonPayNow = dialog.findViewById(R.id.PayNow);
                Button buttonCashPay = dialog.findViewById(R.id.Cash);
                buttonPayNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Address = editTextAddress.getText().toString();
                        if (Address.equals(null) || Address.equals("")) {
                            editTextAddress.setError("Invalid Delivery Address");
                        } else {
                            if (user.getDisplayName().equals(null) || user.getDisplayName().equals("")) {
                                Toast.makeText(OrderActivity.this, "Please Complete your Profile", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(OrderActivity.this, EditProfileActivity.class);
                                startActivity(intent);
                            } else
                                PayNow();
                            dialog.dismiss();
                        }
                    }
                });

                buttonCashPay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Address = editTextAddress.getText().toString();
                        if (Address.equals(null) || Address.equals("")) {
                            editTextAddress.setError("Invalid Delivery Address");
                        } else {

                            if (user.getDisplayName().equals(null) || user.getDisplayName().equals("")) {
                                Toast.makeText(OrderActivity.this, "Please Complete your Profile", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(OrderActivity.this, EditProfileActivity.class);
                                startActivity(intent);
                            } else
                                CashPay();
                            dialog.dismiss();
                        }
                    }
                });

                dialog.show();
            }
        });
    }

    private void CashPay(){
        Orders order = new Orders(
                controller.getFromCard(),
                Double.parseDouble(paymentAmount.getText().toString()),
                "Pending",
                "Pending",
                Address,
                user.getDisplayName(),
                user.getPhoneNumber(),
                System.currentTimeMillis()
        );
        DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference("Order");
        DatabaseReference pushedPostRef = mDatabase.push();
        // Get the unique ID generated by a push()
        String postId = pushedPostRef.getKey();
        pushedPostRef.setValue(order);
        FirebaseDatabase.getInstance().getReference("User").child(user.getUid()).push().setValue(postId);
        Toast.makeText(OrderActivity.this, "Order successfully Booked.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void PayNow(){

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "7879793064@ybl")
                .appendQueryParameter("pn", "Yogesh Kumar")
                .appendQueryParameter("tn", "")
                .appendQueryParameter("am", paymentAmount.getText().toString())
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(OrderActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.d("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.d("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(OrderActivity.this)) {
            String str = data.get(0);
            Log.d("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Orders order = new Orders(
                        controller.getFromCard(),
                        Double.parseDouble(paymentAmount.getText().toString()),
                        "Done",
                        "Pending",
                        Address,
                        user.getDisplayName(),
                        user.getPhoneNumber(),
                        System.currentTimeMillis()
                );

                DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference("Order");
                DatabaseReference pushedPostRef = mDatabase.push();
                // Get the unique ID generated by a push()
                String postId = pushedPostRef.getKey();
                pushedPostRef.setValue(order);
                FirebaseDatabase.getInstance().getReference("User").child(user.getUid()).push().setValue(postId);
                Toast.makeText(OrderActivity.this, "Order successfully Booked.", Toast.LENGTH_SHORT).show();
                finish();
                Log.d("UPI", "responseStr: "+approvalRefNo);
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(OrderActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(OrderActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(OrderActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}