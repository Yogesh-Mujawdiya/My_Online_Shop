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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class AddCategoryActivity extends AppCompatActivity {

    TextInputEditText editTextName;
    ImageView imageViewCategory;
    Button SaveData;
    private static final int SELECT_PICTURE = 100;
    private boolean flag;
    private Uri FilePath = null;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://golu-online-shop.appspot.com/Category/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        editTextName = findViewById(R.id.Name);
        imageViewCategory = findViewById(R.id.CategoryIcon);
        SaveData = findViewById(R.id.SaveBtn);


        imageViewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Category Icon"),SELECT_PICTURE );
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
        final String category = editTextName.getText().toString();
        final ProgressDialog progressDialog = new ProgressDialog(AddCategoryActivity.this);
        if(FilePath == null){
            Toast.makeText(AddCategoryActivity.this, "Please Select Valid Category Icon", Toast.LENGTH_SHORT).show();
        }
        else if(category.equals(null) || category.equals("")){
            editTextName.setError("Please Enter a Valid Category Name");
        }
        else {
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            flag = true;
            DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference("Category/"+category);
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(flag && dataSnapshot.hasChild("Count")){
                        Toast.makeText(AddCategoryActivity.this, "Category Name is already Available", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        flag = false;
                        StorageReference riversRef = storageRef.child(category);
                        riversRef.putFile(FilePath)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressDialog.dismiss();
                                        DatabaseReference Database  = FirebaseDatabase.getInstance().getReference("Category/");
                                        Database.child(category).child("Count").setValue(0);
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
                                        progressDialog.setMessage("Uploading Category Icon" + ((int) progress) + "%...");
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