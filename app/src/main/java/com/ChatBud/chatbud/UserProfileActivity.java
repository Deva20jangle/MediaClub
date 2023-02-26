package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.databinding.ActivityUserProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    ActivityUserProfileBinding binding;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String receiverID = intent.getStringExtra("userID");
        String userProfile = intent.getStringExtra("userProfile");
        String bio = intent.getStringExtra("bio");
        String mobile = intent.getStringExtra("phNo");

        binding.Msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfileActivity.this, ChatsActivity.class)
                        .putExtra("userID",receiverID)
                        .putExtra("userProfile",userProfile)
                        .putExtra("userName",userName));
                finish();
            }
        });

        binding.pCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserProfileActivity.this, "Voice Call", Toast.LENGTH_SHORT).show();
            }
        });

        binding.vCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserProfileActivity.this, "Video Call", Toast.LENGTH_SHORT).show();
            }
        });

        if (receiverID != null) {
            binding.toolbar.setTitle(userName);
            binding.toolbar.setTitleTextColor(Color.WHITE);


            Log.d("TAG", "onCreate: Mobile: " + mobile);
            Log.d("TAG", "onCreate: Bio: " + bio);
            Log.d("TAG", "onCreate: userID: " + receiverID);

            if (bio == null) {
                binding.tvDesc.setText("Hey, This is ChatBud.");
            } else {
                binding.tvDesc.setText(bio);
            }
            binding.tvPhone.setText(mobile);
            if (userProfile.isEmpty()) {
                binding.imageProfile.setImageResource(R.drawable.icon_male_ph);
            } else {
                Picasso.get().load(userProfile).into(binding.imageProfile);
            }
        }

    }

}