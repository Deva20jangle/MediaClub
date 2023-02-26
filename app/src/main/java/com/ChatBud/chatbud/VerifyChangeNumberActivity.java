package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.R;
import com.ChatBud.chatbud.databinding.ActivityVerifyChangeNumberBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class VerifyChangeNumberActivity extends AppCompatActivity {

    ActivityVerifyChangeNumberBinding binding;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;

    String phone, newph, oldph;
    String c1, c2, c3, c4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyChangeNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null)
        {
            firestore.collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    phone = Objects.requireNonNull(documentSnapshot.get("userPhone")).toString();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(VerifyChangeNumberActivity.this, "Failed to get your contact number. Please try again later."+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        binding.ccp1.registerCarrierNumberEditText(binding.edOldPhone);
        binding.ccp2.registerCarrierNumberEditText(binding.edNewPhone);

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.edOldPhone.getText().toString().isEmpty())
                {
                    binding.edOldPhone.setError("Enter phone number.");
                    return;
                }
                if (binding.edNewPhone.getText().toString().isEmpty())
                {
                    binding.edNewPhone.setError("Enter phone number.");
                    return;
                }



                String oldno = binding.ccp1.getFullNumberWithPlus().replace(" ", "");
                String newno = binding.ccp2.getFullNumberWithPlus().replace(" ", "");

                phone = phone.replace(" ", "");

                Log.d("TAG", "onCreate: Current User Phone Number::"+phone);
                Log.d("TAG", "onCreate: Current User old Number::"+oldno);
                Log.d("TAG", "onCreate: Current User new Number::"+newno);

                if (oldno.equals(newno)){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(VerifyChangeNumberActivity.this);
                    builder.setMessage("You have entered same contact number on both side.");
                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else if (!phone.equals(oldno)){
                    binding.edOldPhone.setError("Entered number doesn't match to your current number");
                }else if (phone.equals(newno)){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(VerifyChangeNumberActivity.this);
                    builder.setMessage("You are already using this contact number.");
                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else {
                    Intent intent = new Intent(VerifyChangeNumberActivity.this, OTPVerifyChangeNumberActivity.class);
                    intent.putExtra("oldNumber", binding.ccp1.getFullNumberWithPlus().replace(" ", ""));
                    intent.putExtra("newNumber", binding.ccp2.getFullNumberWithPlus().replace(" ", ""));
                    startActivity(intent);
                }

            }
        });
    }
}