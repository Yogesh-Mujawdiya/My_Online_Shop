package com.my.online_shop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.my.online_shop.Adapter.CategoryAdapter;
import com.my.online_shop.Class.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    String CategoryName;
    TextInputEditText editTextName, editTextPrice;
    ImageView imageViewCategory;
    Button SaveData;

    private boolean flag;
    private static final int SELECT_PICTURE = 100;

    private Uri FilePath = null;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://golu-online-shop.appspot.com/Product/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CategoryName = getIntent().getStringExtra("Category");

        setContentView(R.layout.activity_add_product);
        editTextName = findViewById(R.id.Name);
        editTextPrice = findViewById(R.id.Price);
        imageViewCategory = findViewById(R.id.ProductIcon);
        SaveData = findViewById(R.id.SaveBtn);

        imageViewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Product Icon"),SELECT_PICTURE );
            }
        });

        SaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveUserData();
            }
        });
    }

    private void SaveUserData(){
        final String product = editTextName.getText().toString();
        final String price = editTextPrice.getText().toString();
        final ProgressDialog progressDialog = new ProgressDialog(AddProductActivity.this);
        if(FilePath == null){
            Toast.makeText(AddProductActivity.this, "Please Select Valid Product Icon", Toast.LENGTH_SHORT).show();
        }
        else if(product.equals(null) || product.equals("")){
            editTextName.setError("Invalid Product Name");
        }
        else if(price.equals(null) || price.equals("")){
            editTextName.setError("Invalid Product Price");
        }
        else {
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            final DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference("Category/"+CategoryName);
            flag = true;
            mDatabase.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final int count = Integer.parseInt(dataSnapshot.child("Count").getValue().toString())+1;
                    if(flag && dataSnapshot.child("Product").hasChild(product)){
                        Toast.makeText(AddProductActivity.this, "Product Name is already Available", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        StorageReference riversRef = storageRef.child(product);
                        riversRef.putFile(FilePath)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressDialog.dismiss();
                                        mDatabase.child("Count").setValue(count);
                                        DatabaseReference Database  = FirebaseDatabase.getInstance().getReference("Category/"+CategoryName+"/Product/"+product);
                                        Database.child("Price").setValue(price);

                                        Toast.makeText(AddProductActivity.this, "Successfully Add Data", Toast.LENGTH_SHORT).show();
                                        flag = false;
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        progressDialog.setMessage("Uploading Product Icon" + ((int) progress) + "%...");
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressDialog.dismiss();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            FilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePath);
                imageViewCategory.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}