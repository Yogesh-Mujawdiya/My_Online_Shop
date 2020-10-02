package com.my.online_shop.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.my.online_shop.BuildConfig;
import com.my.online_shop.Controller.StoreData;
import com.my.online_shop.EditProfileActivity;
import com.my.online_shop.LoginActivity;
import com.my.online_shop.R;

public class ProfileFragment extends Fragment {

    StoreData controller;
    ImageView UserProfile, EditProfile;
    TextView UserName, UserEmail, UserMobile, Logout;
    RelativeLayout HelpCenter, ShareApp, RateUs;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseUser user;
    StorageReference storageRef = storage.getReferenceFromUrl("gs://golu-online-shop.appspot.com/User/");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        UserProfile = root.findViewById(R.id.UserProfile);
        EditProfile = root.findViewById(R.id.EditProfile);
        UserName = root.findViewById(R.id.UserName);
        UserEmail = root.findViewById(R.id.UserEmail);
        UserMobile = root.findViewById(R.id.UserMobile);
        Logout = root.findViewById(R.id.Logout);
//        HelpCenter = root.findViewById(R.id.HelpCenter);
        ShareApp = root.findViewById(R.id.ShareApp);
        RateUs = root.findViewById(R.id.RateUs);

        controller = new StoreData(getContext());

        user = FirebaseAuth.getInstance().getCurrentUser();
        UserName.setText(user.getDisplayName());
        UserEmail.setText(user.getEmail());
        UserMobile.setText(user.getPhoneNumber());

        storageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("UserData",uri.toString());
                Glide.with(getActivity()).load(uri).into(UserProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });

        ShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                    String shareMessage= "\nLet me recommend you this application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });

        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });



        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return root;
    }
}