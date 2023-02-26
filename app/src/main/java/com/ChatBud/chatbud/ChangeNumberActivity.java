package com.ChatBud.chatbud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ChatBud.chatbud.databinding.ActivityChangeNumberBinding;

public class ChangeNumberActivity extends AppCompatActivity {

    ActivityChangeNumberBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangeNumberActivity.this, VerifyChangeNumberActivity.class);
                startActivity(intent);
            }
        });

    }
}