package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.databinding.ActivityVerifyOtpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class VerifyOTPActivity extends AppCompatActivity {

    ActivityVerifyOtpBinding binding;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;
    String phNo, otpid;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        phNo = getIntent().getStringExtra("mobile").toString();
        binding.textView.setText(phNo);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(VerifyOTPActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("We're verifying the OTP.");

        initiateotp();

        binding.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.edVerify.getText().toString().isEmpty())
                {
                    Toast.makeText(VerifyOTPActivity.this, "Blank field can not be processed.", Toast.LENGTH_SHORT).show();
                }
                else if (binding.edVerify.getText().toString().length() != 6)
                {
                    Toast.makeText(VerifyOTPActivity.this, "Invalid Otp.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    try {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpid, binding.edVerify.getText().toString());
                        signInWithPhoneAuthCredential(credential);
                    }catch (Exception e){
                        Toast.makeText(VerifyOTPActivity.this, "Something went wrong. please try again later."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void initiateotp() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phNo
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

                        signInWithPhoneAuthCredential(phoneAuthCredential);

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                        Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        //progressDialog.show();

        Log.d("credentials", "credentials :: " + credential);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            FirebaseUser user = task.getResult().getUser();

                            if (user != null)
                            {
                                String contact_no = user.getPhoneNumber();
                                String c1, c2;
                                String c3 = user.getPhoneNumber();

                                //check contact_no contains +91 or not
                                if (contact_no.contains("+91")){
                                    Log.d("TAG", "onSuccess: ------------------------------------------------------------------------------------------");
                                    Log.d("TAG", "onClick: contact number lenght before split: "+contact_no.length());

                                    c1 = contact_no.substring(3, 8);
                                    c2 = contact_no.substring(8, 13);
                                    c3 = c1+" "+c2;

                                    Log.d("contact_no", "inside if contact_no :: " + contact_no);

                                    Log.d("TAG", "onSuccess: ------------------------------------------------------------------------------------------");
                                    Log.d("TAG", "onClick: contact number lenght after splt: "+contact_no.length());

                                    Log.d("TAG", "onSuccess: ------------------------------------------------------------------------------------------");
                                    Log.d("TAG", "onComplete: contact number lenght Contact number: "+c1+" "+c2);

                                    Log.d("TAG", "onSuccess: ------------------------------------------------------------------------------------------");
                                    Log.d("TAG", "onComplete: contact number lenght Contact number c3 :: "+c3);
                                }

                                Log.d("contact_no", "contact_no :: " + contact_no);


                                //progressDialog.dismiss();
                                startActivity(new Intent(VerifyOTPActivity.this, SetUserInfoActivity.class));
                                finish();
                                /*String userId = user.getUid();
                                Users users = new Users(userId,
                                        "",
                                        c3,
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "");


                                firestore.collection("Users").document("UserInfo").collection(userId)
                                        .add(users).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        progressDialog.dismiss();
                                        startActivity(new Intent(VerifyOTPActivity.this, SetUserInfoActivity.class));
                                        finish();
                                    }
                                });*/
                            }
                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(VerifyOTPActivity.this, "Something went wrong. please try again later or Contact us.", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else
                        {
                            progressDialog.dismiss();
                            Toast.makeText(VerifyOTPActivity.this, "Sign in Error.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}