package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.databinding.ActivityOtpVerifyChangeNumberBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class OTPVerifyChangeNumberActivity extends AppCompatActivity {

    ActivityOtpVerifyChangeNumberBinding binding;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;
    String oldphNo, newphNo, otpid;
    String c1, c2, c3, c4;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerifyChangeNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        oldphNo = getIntent().getStringExtra("oldNumber").toString();
        binding.txtOldNumber.setText(oldphNo);

        newphNo = getIntent().getStringExtra("newNumber").toString();
        binding.txtNewNumber.setText(newphNo);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(OTPVerifyChangeNumberActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("We're verifying the OTP.");

        initiateotp();

        binding.btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.edVerify.getText().toString().isEmpty()) {
                    Toast.makeText(OTPVerifyChangeNumberActivity.this, "Blank field can not be processed.", Toast.LENGTH_SHORT).show();
                } else if (binding.edVerify.getText().toString().length() != 6) {
                    Toast.makeText(OTPVerifyChangeNumberActivity.this, "Invalid Otp.", Toast.LENGTH_SHORT).show();
                } else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpid, binding.edVerify.getText().toString());
                    if (newphNo.contains("+91")) {
                        c1 = newphNo.substring(0, 3);
                        c2 = newphNo.substring(3, 8);
                        c3 = newphNo.substring(8, 13);
                        c4 = c1 + " " + c2 + " " + c3;
                    }

                    Log.d("TAG", "onCreate: Current User new Number c4::" + c4);
                    PhoneAuthCredential credential1 = PhoneAuthProvider.getCredential(otpid, binding.edVerify.getText().toString());
                    changePhoneNumber(credential1);
                }
            }
        });


        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void initiateotp() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(newphNo
                , 60
                , TimeUnit.SECONDS
                , this
                , new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        otpid = s;
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                        Toast.makeText(OTPVerifyChangeNumberActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void changePhoneNumber(PhoneAuthCredential phoneAuthCredential) {

        String newPhone = getIntent().getStringExtra("newNumber").toString();

        progressDialog.show();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                        String userId = snapshots.getString("userID");
                        String phone = snapshots.getString("userPhone");

                        Log.d("TAG", "onCreate: Current User Numbers ::" + phone);

                        Users user = new Users();
                        user.setUserPhone(phone);
                        user.setUserID(userId);

                        if (phone.equals(c4) && userId.equals(firebaseUser.getUid())) {
                            sameNumber();
                        } else if (phone.equals(c4) && !userId.equals(firebaseUser.getUid())) {
                            alreadyUsingNumber();
                        } else if (!phone.equals(c4) && userId.equals(firebaseUser.getUid())) {


                            FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
                            auth.updateCurrentUser(user1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    user1.updatePhoneNumber(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                firestore.collection("Users").document(firebaseUser.getUid()).update("userPhone", c4).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        progressDialog.dismiss();
                                                        Intent intent = new Intent(OTPVerifyChangeNumberActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                        Toast.makeText(OTPVerifyChangeNumberActivity.this, "Phone Number changed successfully.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                progressDialog.dismiss();
                                                Intent intent = new Intent(OTPVerifyChangeNumberActivity.this, ChangeNumberActivity.class);
                                                startActivity(intent);
                                                Toast.makeText(OTPVerifyChangeNumberActivity.this, "Something went wrong... Please Try again Later.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(OTPVerifyChangeNumberActivity.this, ChangeNumberActivity.class);
                                            startActivity(intent);
                                            Toast.makeText(OTPVerifyChangeNumberActivity.this, "Failed updatePhoneNumber." +e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(OTPVerifyChangeNumberActivity.this, ChangeNumberActivity.class);
                                    startActivity(intent);
                                    Toast.makeText(OTPVerifyChangeNumberActivity.this, "Failed updateCurrentUser." +e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            });
        }
    }


    private void alreadyUsingNumber() {
        progressDialog.dismiss();
        final AlertDialog.Builder builder = new AlertDialog.Builder(OTPVerifyChangeNumberActivity.this);
        builder.setMessage("Someone is already using this contact number.");
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(OTPVerifyChangeNumberActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void sameNumber() {
        progressDialog.dismiss();
        final AlertDialog.Builder builder = new AlertDialog.Builder(OTPVerifyChangeNumberActivity.this);
        builder.setMessage("You are already using this contact number.");
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(OTPVerifyChangeNumberActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}