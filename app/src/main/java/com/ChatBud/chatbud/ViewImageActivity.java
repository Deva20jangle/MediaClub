package com.ChatBud.chatbud;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ChatBud.chatbud.Common.Common;
import com.ChatBud.chatbud.databinding.ActivityViewImageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewImageActivity extends AppCompatActivity {

    ActivityViewImageBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.imageView.setImageBitmap(Common.IMAGE_BITMAP);

    }
}