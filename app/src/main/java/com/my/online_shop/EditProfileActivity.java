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
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    ImageView ImageUserProfile;
    TextInputEditText EditTextUserName;
    TextInputEditText EditTextMobile;
    TextInputEditText EditTextEmail;
    private String OldEmail, OldMobile, OldName;
    Button SaveData;
    private static final int SELECT_PICTURE = 100;

    private Uri FilePath = null;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://golu-online-shop.appspot.com/User/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ImageUserProfile = findViewById(R.id.UserProfile);
        EditTextUserName = findViewById(R.id.UserName);
        EditTextEmail = findViewById(R.id.UserEmail);
        EditTextMobile = findViewById(R.id.UserMobile);
        SaveData = findViewById(R.id.SaveBtn);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            EditTextMobile.setText(user.getPhoneNumber());
            EditTextMobile.setEnabled(false);
            EditTextUserName.setText(user.getDisplayName());
            OldName = user.getDisplayName();
            EditTextEmail.setText(user.getEmail());
            OldEmail = user.getEmail();
            storageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d("UserData",uri.toString());
                    Glide.with(getApplicationContext()).load(uri).into(ImageUserProfile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exception.printStackTrace();
                }
            });
        }
        else{
            Intent homeIntent = new Intent(EditProfileActivity.this, LoginActivity.class);
            startActivity(homeIntent);
            getParent().finish();
            finish();
        }

        ImageUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Profile Picture"),SELECT_PICTURE );
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
        final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            return;
        }
        if(OldName==null || !OldName.equals(EditTextUserName.getText().toString())){
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(EditTextUserName.getText().toString())
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditProfileActivity.this, "User Name updated.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        if(OldEmail == null || OldEmail == "" || !OldEmail.equals(EditTextEmail.getText().toString())) {
            user.updateEmail("ykmujawdiya@gmail.com")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Success", "User email address updated.");
                                Toast.makeText(EditProfileActivity.this, "User email address updated.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(EditProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        if(FilePath != null) {
            StorageReference riversRef = storageRef.child(user.getUid());
            riversRef.putFile(FilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
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
                            progressDialog.setMessage("Uploading Profile Pic" + ((int) progress) + "%...");
                        }
                    });
        }
        else {
            progressDialog.dismiss();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            FilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePath);
                ImageUserProfile.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
