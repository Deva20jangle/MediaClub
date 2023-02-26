package com.ChatBud.chatbud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ChatBud.chatbud.databinding.ActivityPhoneLoginBinding;

public class PhoneLoginActivity extends AppCompatActivity {

    ActivityPhoneLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ccp.registerCarrierNumberEditText(binding.edPhoneNo);

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.edPhoneNo.getText().toString().isEmpty())
                {
                    binding.edPhoneNo.setError("Enter phone number.");
                    return;
                }

                Intent intent = new Intent(PhoneLoginActivity.this, VerifyOTPActivity.class);
                intent.putExtra("mobile", binding.ccp.getFullNumberWithPlus().replace(" ", ""));
                startActivity(intent);
                finishAffinity();
            }
        });
    }
}