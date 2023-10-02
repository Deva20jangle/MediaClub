package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ChatBud.chatbud.databinding.ActivitySplashScreenBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

public class SplashScreenActivity extends AppCompatActivity {

    ActivitySplashScreenBinding binding;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        if (getIntent().getExtras() != null){
            int size = getIntent().getExtras().size();
            Log.d ("intentData", size + " is a size in the bundle");
        }
        if (user != null && getIntent().getExtras() != null && getIntent().getExtras().size() != 1){
            String userID = getIntent().getExtras().getString("userID");
            Log.d("TAG", "onComplete: FCMToken Splash screen userId from notification:: " + userID);
            firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentReference docRef = firebaseFirestore.collection("Users").document(userID);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        String userName = document.getString("userName");
                                        String Bio = document.getString("Bio");
                                        String phoneNo = document.getString("phoneNo");
                                        String userProfile = document.getString("userProfile");

                                        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                                        startActivity(intent);

                                        SplashScreenActivity.this.startActivity(new Intent(SplashScreenActivity.this, ChatsActivity.class)
                                                .putExtra("userID",userID)
                                                .putExtra("userName",userName)
                                                .putExtra("Bio", Bio)
                                                .putExtra("phoneNo", phoneNo)
                                                .putExtra("userProfile",userProfile));
                                        finish();
                                        getIntent().replaceExtras(new Bundle());
                                    } else {
                                        Log.d("TAG", "No such document");
                                    }
                                } else {
                                    Log.d("TAG", "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                }
            });
            getIntent().replaceExtras(new Bundle());
        } else {
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
}