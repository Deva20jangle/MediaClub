package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.databinding.ActivitySetUserInfoBinding;
import com.ChatBud.chatbud.databinding.ActivityVerifyOtpBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class SetUserInfoActivity extends AppCompatActivity {

    ActivitySetUserInfoBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;
    ProgressDialog progressDialog, pd;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    String bio, imageProfileDB;
    String myDeviceID;

    CountDownTimer countDownTimer;

    BottomSheetDialog bottomSheetDialog;
    private Uri image_uri = null;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //getLoginStatus();

        myDeviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        //Check ,is the user new or not
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        db = FirebaseFirestore.getInstance();
        db.collection("Users").document(firebaseUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    bio = Objects.requireNonNull(task.getResult()).getString("bio");
                    imageProfileDB = Objects.requireNonNull(task.getResult()).getString("imageProfile");

                    binding.edName.setText(task.getResult().getString("userName"));

                    if (bio == null) {
                        binding.edStatus.setText("Hey, This is ChatBud.");
                    } else {
                        binding.edStatus.setText(bio);
                    }

                    if (imageProfileDB == null) {

                        binding.imageProfile.setImageResource(R.drawable.icon_male_ph);

                    } else {

                        Glide.with(SetUserInfoActivity.this).load(imageProfileDB).into(binding.imageProfile);

                    }

                } else {
                    Log.w("TAG", "Error getting documents.", task.getException());
                }
            }
        });

        progressDialog = new ProgressDialog(this);
        pd = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");

        binding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.edName.getText().toString().isEmpty()) {
                    Toast.makeText(SetUserInfoActivity.this, "Please enter the username.", Toast.LENGTH_SHORT).show();
                }

                //PhoneAuthCredential credential = getIntent().getStringExtra()

                //doUpdate
                progressDialog.show();
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    String userId = firebaseUser.getUid();

                    String contact_no = firebaseUser.getPhoneNumber();
                    String c1, c2, c3, mobile;
                    String c4 = firebaseUser.getPhoneNumber();

                    //check contact_no contains +91 or not
                    if (contact_no.contains("+91")) {
                        Log.d("TAG", "onSuccess: ------------------------------------------------------------------------------------------");
                        Log.d("TAG", "onClick: contact number lenght before split: " + contact_no.length());

                        c1 = contact_no.substring(0, 3);
                        c2 = contact_no.substring(3, 8);
                        c3 = contact_no.substring(8, 13);
                        c4 = c1 + " " + c2 + " " + c3;
                        mobile = c2 + c3;

                        Intent intent = new Intent(SetUserInfoActivity.this, ContactsActivity.class);
                        intent.putExtra("mob", mobile);

                        Log.d("contact_no", "inside if contact_no :: " + contact_no);

                        Log.d("TAG", "onSuccess: ------------------------------------------------------------------------------------------");
                        Log.d("TAG", "onClick: contact number lenght after splt: " + contact_no.length());

                        Log.d("TAG", "onSuccess: ------------------------------------------------------------------------------------------");
                        Log.d("TAG", "onComplete: contact number length Contact number: " + c1);
                        Log.d("TAG", "onComplete: contact number length Contact number: " + c2);
                        Log.d("TAG", "onComplete: contact number length Contact number: " + c3);

                        Log.d("TAG", "onComplete: contact number length Contact number: " + c4);
                    }

                    String userName = binding.edName.getText().toString();
                    String userPhone = c4;
                    String bio = binding.edStatus.getText().toString();

                    if (image_uri == null) {
                        setInfo("" + userId,
                                "" + userName,
                                "" + userPhone,
                                "" + bio,
                                "");
                    } else {

                        String fileNameAndPath = "/ImagesProfile/" + "" + userId;

                        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
                        storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!p_uriTask.isSuccessful()) ;
                                Uri p_downloadUri = p_uriTask.getResult();
                                if (p_uriTask.isSuccessful()) {
                                    setInfo("" + userId,
                                            "" + userName,
                                            "" + userPhone,
                                            "" + bio,
                                            "" + p_downloadUri);
                                }
                            }
                        });
                    }

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(SetUserInfoActivity.this, "Something went wrong! Failed to update user.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void getLoginStatus() {

        countDownTimer = new CountDownTimer(5000, 100) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                countDownTimer.start();
                firebaseFirestore.collection("Users")
                        .document(firebaseUser.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String deviceID = documentSnapshot.get("deviceID").toString();
                        if (!deviceID.equals(myDeviceID)) {

                            Log.d("TAG", "onSuccess: DeviceID database :: " + deviceID);
                            Log.d("TAG", "onSuccess: DeviceID Phone :: " + myDeviceID);

                            //auth.signOut();
                            pd.setTitle("Logging out");
                            pd.setMessage("Your account is logged in another device. If it is not you then contact to us.");
                            pd.setCancelable(false);

                            pd.show();
                            auth.signOut();
                            pd.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetUserInfoActivity.this, "Error in single device login" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "onFailure: Error in single device login" + e.getMessage());
                    }
                });
            }
        }.start();

    }

    private void setInfo(String userId, String userName, String userPhone, String bio, String imageProfile) {

        HashMap<String, String> hashmap = new HashMap<>();
        hashmap.put("userID", "" + userId);
        hashmap.put("userName", "" + userName);
        hashmap.put("userPhone", "" + userPhone);
        hashmap.put("imageProfile", imageProfile);
        hashmap.put("imageCover", "");
        hashmap.put("email", "");
        hashmap.put("dateOfBirth", "");
        hashmap.put("gender", "");
        hashmap.put("status", "");
        hashmap.put("deviceID", "" + myDeviceID);
        hashmap.put("bio", "" + bio);

        firebaseFirestore.collection("Users").document(firebaseUser.getUid()).set(hashmap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(SetUserInfoActivity.this, "User updated successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SetUserInfoActivity.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                });

    }

    private void showImagePickDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick, null);

        ((View) view.findViewById(R.id.lnGallery)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
                bottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.lnCamera)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermissions();
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog = new BottomSheetDialog(SetUserInfoActivity.this);
        bottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Objects.requireNonNull(bottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                bottomSheetDialog = null;
            }
        });

        bottomSheetDialog.show();
    }

    private void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    221);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    222);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Image Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                binding.imageProfile.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                binding.imageProfile.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}