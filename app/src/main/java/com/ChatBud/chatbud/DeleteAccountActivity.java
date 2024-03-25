package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ChatBud.chatbud.databinding.ActivityDeleteAccountBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeleteAccountActivity extends AppCompatActivity {

    ActivityDeleteAccountBinding binding;
    FirebaseUser user;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.btnChangeNO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeleteAccountActivity.this, ChangeNumberActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.btnDeleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(DeleteAccountActivity.this);
                builder.setMessage("Do you want to delete your account?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phone = user.getPhoneNumber();
                        if (phone.contains("+91")){
                            phone = phone.substring(3, 13);
                        }

                        Log.d("TAG", "Phone: Database Phone: "+phone);
                        Log.d("TAG", "Phone: Entered Phone: "+binding.edPhoneNo.getText().toString());

                        if (phone.equals(binding.edPhoneNo.getText().toString())) {
                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        firestore.collection("Users").document(user.getUid())
                                                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        dialog.cancel();
                                                        FirebaseAuth.getInstance().signOut();
                                                        Intent intent = new Intent(DeleteAccountActivity.this, StartActivity.class);
                                                        startActivity(intent);
                                                        finishAffinity();
                                                        Toast.makeText(DeleteAccountActivity.this, "Your account has been deleted successfully.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(DeleteAccountActivity.this, "Failed to delete account data."+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(DeleteAccountActivity.this, "Failed to delete document.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(DeleteAccountActivity.this, "Failed to delete account."+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(DeleteAccountActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();


            }
        });
    }
}