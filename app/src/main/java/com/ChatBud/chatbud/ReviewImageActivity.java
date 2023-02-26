package com.ChatBud.chatbud;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ChatBud.chatbud.databinding.ActivityReviewImageBinding;
import com.bumptech.glide.Glide;

public class ReviewImageActivity extends AppCompatActivity {

    ActivityReviewImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String image = getIntent().getStringExtra("image");

        Log.d("TAG", "onCreate: image URL: "+image);

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Glide.with(this).load(image).into(binding.imageView);

    }
}