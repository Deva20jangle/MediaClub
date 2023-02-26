package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.ChatBud.chatbud.Common.Common;
import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.databinding.ActivityProfileBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;
    ProgressDialog progressDialog;

    BottomSheetDialog bottomSheetDialog, bsDialogEditname, bsDialogEditbio;

    private int IMAGE_GALLERY_REQUEST = 111;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (firebaseUser != null)
        {
            getInfo();
        }//end getInfo

        binding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        checkCameraPermission();
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog = new BottomSheetDialog(ProfileActivity.this);
                bottomSheetDialog.setContentView(view);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Objects.requireNonNull(bottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }

                bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        bottomSheetDialog=null;
                    }
                });

                bottomSheetDialog.show();
            }
        });
        //end of initActionClick && showBottomPickPhoto

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageProfile.invalidate();
                if (binding.imageProfile.getDrawable() != null){
                    if (binding.imageProfile.getDrawable().getCurrent() instanceof GlideBitmapDrawable){
                        Common.IMAGE_BITMAP = ((GlideBitmapDrawable)binding.imageProfile.getDrawable().getCurrent()).getBitmap();
                    } else {
                        Common.IMAGE_BITMAP = ((BitmapDrawable)binding.imageProfile.getDrawable().getCurrent()).getBitmap();
                    }
                } else {
                    Common.IMAGE_BITMAP = BitmapFactory.decodeResource(ProfileActivity.this.getResources(),
                            R.drawable.icon_male_ph);
                }

                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this, binding.imageProfile, "image");
                Intent intent = new Intent(ProfileActivity.this, ViewImageActivity.class);
                startActivity(intent, activityOptionsCompat.toBundle());
            }
        });

        binding.lneditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_name, null);

                ((View) view.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bsDialogEditname.dismiss();
                    }
                });
                final EditText edUserName = view.findViewById(R.id.eduserName);
                ((View) view.findViewById(R.id.btnSave)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (edUserName.getText().toString().isEmpty())
                        {
                            Toast.makeText(ProfileActivity.this, "Enter username first.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else
                        {
                            updateName(edUserName.getText().toString());
                            bsDialogEditname.dismiss();

                            Toast.makeText(ProfileActivity.this, "Username has been updated successfully..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                bsDialogEditname = new BottomSheetDialog(ProfileActivity.this);
                bsDialogEditname.setContentView(view);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Objects.requireNonNull(bsDialogEditname.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }

                bsDialogEditname.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        bsDialogEditname=null;
                    }
                });

                bsDialogEditname.show();
            }
        });

        binding.lneditBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_bio, null);

                ((View) view.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bsDialogEditbio.dismiss();
                    }
                });
                final EditText edUserBio = view.findViewById(R.id.eduserBio);
                ((View) view.findViewById(R.id.btnSave)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateBio(edUserBio.getText().toString());
                        bsDialogEditbio.dismiss();
                    }
                });

                bsDialogEditbio = new BottomSheetDialog(ProfileActivity.this);
                bsDialogEditbio.setContentView(view);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Objects.requireNonNull(bsDialogEditbio.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }

                bsDialogEditbio.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        bsDialogEditbio=null;
                    }
                });

                bsDialogEditbio.show();
            }
        });
        //end of bottomSheetEditName bsDialogEditbio

        binding.btnEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChangeNumberActivity.class);
                startActivity(intent);
            }
        });

    }

    private void checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    221);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    222);
        }
        else {
            openCamera();
        }
    }

    private void openCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);

            Log.d("TAG", "openCamera: imageFileName: "+imageFileName);
            Log.d("TAG", "openCamera: imageURI: "+imageUri);

            
            intent.putExtra(MediaStore.EXTRA_OUTPUT,  imageUri);
            intent.putExtra("listPhotoName", imageFileName);
            startActivityForResult(intent, 440);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getInfo() {
        firestore.collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.getString("userName");
                String phone = documentSnapshot.getString("userPhone");
                String bio = documentSnapshot.getString("bio");
                String imageProfile = documentSnapshot.getString("imageProfile");

                binding.txtuserName.setText(userName);
                binding.txtPhone.setText(phone);


                if (bio.isEmpty()) {
                    binding.txtAbout.setText("Hey, This is ChatBud.");
                } else{
                    binding.txtAbout.setText(bio);
                }

                if (imageProfile.isEmpty()) {
                    binding.imageProfile.setImageResource(R.drawable.icon_male_ph);
                } else{
                   // Picasso.get().load(imageProfile).into(binding.imageProfile);
                    Glide.with(ProfileActivity.this).load(imageProfile).into(binding.imageProfile).onLoadFailed(new Exception("failed"),
                            ContextCompat.getDrawable(ProfileActivity.this, R.drawable.icon_male_ph));
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void openGallery(){
       /* Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_GALLERY_REQUEST);*/

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (imageUri != null)
            {
                progressDialog.setMessage("Uploading...");
                progressDialog.show();

                StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("ImagesProfile").child(FirebaseAuth.getInstance().getUid());
                riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        Uri downloadUrl = urlTask.getResult();

                        final String sdownload_url = String.valueOf(downloadUrl);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageProfile", sdownload_url);



                        progressDialog.dismiss();
                        firestore.collection("Users").document(firebaseUser.getUid()).update(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),"upload successfully",Toast.LENGTH_SHORT).show();
                                        getInfo();
                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"upload Failed",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.imageProfile.setImageBitmap(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if (requestCode == 440 && resultCode == RESULT_OK){
            uploadToFirebase();
        }

    }

    private String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadToFirebase(){
        if (imageUri!=null){
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("ImagesProfile").child(FirebaseAuth.getInstance().getUid());
            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    final String sdownload_url = String.valueOf(downloadUrl);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("imageProfile", sdownload_url);


                    progressDialog.dismiss();
                    firestore.collection("Users").document(firebaseUser.getUid()).update(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(),"upload successfully",Toast.LENGTH_SHORT).show();

                                    getInfo();
                                }
                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"upload Failed",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void updateName(String newName){
        firestore.collection("Users").document(firebaseUser.getUid()).update("userName", newName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ProfileActivity.this, "Username updated successfully.", Toast.LENGTH_SHORT).show();
                getInfo();
            }
        });
    }

    private void updateBio(String newBio){
        firestore.collection("Users").document(firebaseUser.getUid()).update("bio", newBio).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ProfileActivity.this, "User bio updated successfully.", Toast.LENGTH_SHORT).show();
                getInfo();
            }
        });
    }
}