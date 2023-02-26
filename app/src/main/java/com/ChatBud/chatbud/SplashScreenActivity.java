package com.ChatBud.chatbud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.ChatBud.chatbud.databinding.ActivitySplashScreenBinding;

public class SplashScreenActivity extends AppCompatActivity {

    ActivitySplashScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Thread thread = new Thread()
        {
            public void run(){
                try
                {
                    sleep(1500);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally
                {
                    Intent intent = new Intent(SplashScreenActivity.this , StartActivity.class);
                    startActivity(intent);
                    finishAffinity();
                }
            }
        };thread.start();
    }
}