package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.ChatBud.chatbud.Adapters.AddParticipantAdapter;
import com.ChatBud.chatbud.Fragments.ChatsFragment;
import com.ChatBud.chatbud.Fragments.GroupsFragment;
import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.databinding.ActivityGroupInfoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class GroupInfoActivity extends AppCompatActivity {

    ActivityGroupInfoBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    FirebaseDatabase database;

    String groupId, uid;
    String myGroupRole = "", role;
    ArrayList<Users> usersList;
    AddParticipantAdapter addParticipantAdapter;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
    BottomSheetDialog editGroupTitle, editGroupDesc, bottomSheetDialog;
    private int IMAGE_GALLERY_REQUEST = 111;
    private Uri imageUri;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(this );

        groupId = getIntent().getStringExtra("groupId");

        loadGroupInfo();
        loadMyGroupRole();

        binding.addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, AddGroupParticipantActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        binding.removePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId);
                ref.child("Participants").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        role = snapshot.child("role").getValue(String.class);
                        
                        if (role.equals("creator") || role.equals("admin")){

                            final AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                            builder.setTitle("Remove Pin");
                            builder.setMessage("Are you sure. Do you want to remove pinned message");
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    ref.child("PinnedMessage").setValue(null);
                                    Toast.makeText(GroupInfoActivity.this, "Pinned message removed.", Toast.LENGTH_SHORT).show();
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

                        } else {
                            Toast.makeText(GroupInfoActivity.this, "You can not remove pinned message.", Toast.LENGTH_SHORT).show();
                        }
                        
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        binding.imgEditGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_group_title, null);

                ((View) view.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editGroupTitle.dismiss();
                    }
                });
                final EditText edNewTitle = view.findViewById(R.id.edNewTitle);
                ((View) view.findViewById(R.id.btnSave)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (edNewTitle.getText().toString().isEmpty()) {
                            Toast.makeText(GroupInfoActivity.this, "Enter group title first. Group title can't be empty.", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            //updateBio(edUserBio.getText().toString());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("groupTitle", ""+edNewTitle.getText().toString());

                            ref.child(groupId).updateChildren(hashMap);
                            editGroupTitle.dismiss();

                            Toast.makeText(GroupInfoActivity.this, "Group name has been updated successfully.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                editGroupTitle = new BottomSheetDialog(GroupInfoActivity.this);
                editGroupTitle.setContentView(view);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Objects.requireNonNull(editGroupTitle.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }

                editGroupTitle.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        editGroupTitle=null;
                    }
                });

                editGroupTitle.show();
            }
        });

        binding.imgEditGroupDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_group_desc, null);

                ((View) view.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editGroupDesc.dismiss();
                    }
                });
                final EditText edNewDesc = view.findViewById(R.id.edNewDesc);
                ((View) view.findViewById(R.id.btnSave)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("groupDescription", ""+edNewDesc.getText().toString());

                        ref.child(groupId).updateChildren(hashMap);
                        editGroupTitle.dismiss();

                        Toast.makeText(GroupInfoActivity.this, "Group description has been updated successfully.", Toast.LENGTH_SHORT).show();
                    }
                });

                editGroupDesc = new BottomSheetDialog(GroupInfoActivity.this);
                editGroupDesc.setContentView(view);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Objects.requireNonNull(editGroupDesc.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }

                editGroupDesc.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        editGroupDesc=null;
                    }
                });

                editGroupDesc.show();
            }
        });

        binding.imgEditGroupIcon.setOnClickListener(new View.OnClickListener() {
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

                bottomSheetDialog = new BottomSheetDialog(GroupInfoActivity.this);
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

        binding.leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(GroupInfoActivity.this).setTitle("Leave Group").setMessage("Are you sure that you want to leave the group.").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Participants").child(user.getUid()).setValue(null);
                            Intent intent = new Intent(GroupInfoActivity.this, MainActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.d("TAG", "onClick: leave failed" + e.getMessage());
                        }

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        binding.deleteGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(GroupInfoActivity.this).setTitle("Delete Group")
                        .setMessage("Deleting the group will delete all the messages and media sent in the group chat \n\nAre you sure that you want to delete the group.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).setValue(null);
                            Intent intent = new Intent(GroupInfoActivity.this, MainActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.d("TAG", "onClick: delete group failed" + e.getMessage());
                        }

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

    }

    private void checkCameraPermission() {
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

    private void openCamera() {
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get group info
                    String groupId = "" + ds.child("groupId").getValue();
                    String groupTitle = "" + ds.child("groupTitle").getValue();
                    String groupDesc = "" + ds.child("groupDescription").getValue();
                    String groupIcon = "" + ds.child("groupIcon").getValue();
                    String createdBy = "" + ds.child("createdBy").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();

                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String dateTime = formatter.format(new Date(Long.parseLong(timestamp)));

                    loadCreatorInfo(createdBy, dateTime);

                    //set group info
                    binding.groupTitleTv.setText(groupTitle);
                    if (groupDesc.isEmpty()) {
                        binding.descriptionTv.setText("Group Description.");
                    } else {
                        binding.descriptionTv.setText(groupDesc);
                    }

                    if (groupIcon.isEmpty()) {
                        binding.groupIconTv.setImageResource(R.drawable.group_icon);
                    } else {
                        Picasso.get().load(groupIcon).into(binding.groupIconTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadCreatorInfo(String createdBy, String dateTime) {

        firestore.collection("Users").document(createdBy).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String creatorName = Objects.requireNonNull(documentSnapshot.get("userName")).toString();
                binding.createdByTv.setText("Created by " + creatorName + " on "+dateTime);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, "unable to load creator name", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadMyGroupRole() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    myGroupRole = "" + ds.child("role").getValue();

                    switch (myGroupRole) {
                        case "participant":
                            binding.addParticipantTv.setVisibility(View.GONE);
                            //binding.editGroupTv.setVisibility(View.GONE);
                            binding.leaveGroupTv.setVisibility(View.VISIBLE);
                            binding.deleteGroupTv.setVisibility(View.GONE);
                            break;
                        case "admin":
                            binding.leaveGroupTv.setVisibility(View.VISIBLE);
                            binding.deleteGroupTv.setVisibility(View.GONE);
                            break;
                        case "creator":
                            //binding.leaveGroupTv.setText("Delete Group");
                            binding.leaveGroupTv.setVisibility(View.GONE);
                            binding.deleteGroupTv.setVisibility(View.VISIBLE);
                            break;
                    }
                }
                loadParticipants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadParticipants() {
        usersList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String uid = ""+ds.child("uid").getValue();

                    usersList.clear();
                    firestore.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()){
                                String userID = Objects.requireNonNull(task.getResult()).getString("userID");
                                String username = task.getResult().getString("userName");
                                String imageProfile = task.getResult().getString("imageProfile");
                                String desc = task.getResult().getString("bio");

                                Users user = new Users();
                                user.setUserID(userID);
                                user.setUserName(username);
                                user.setImageProfile(imageProfile);
                                user.setBio(desc);

                                usersList.add(user);
                            }
                            addParticipantAdapter = new AddParticipantAdapter(GroupInfoActivity.this, usersList, groupId, myGroupRole);
                            binding.participantsRv.setAdapter(addParticipantAdapter);
                            binding.participantsTv.setText("Participants ("+usersList.size()+")");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            Log.d("TAG", "onActivityResult: imageUri: "+imageUri);

            if (imageUri != null)
            {
                progressDialog.setMessage("Uploading...");
                progressDialog.show();

                StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("Group_Profile/").child(FirebaseAuth.getInstance().getUid());
                riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        Uri downloadUrl = urlTask.getResult();

                        final String sdownload_url = String.valueOf(downloadUrl);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("groupIcon", sdownload_url);

                        progressDialog.dismiss();
                        ref.child(groupId).updateChildren(hashMap);

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
                binding.groupIconTv.setImageBitmap(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if (requestCode == 440 && resultCode == RESULT_OK){
            uploadToFirebase();
        }
    }

    private void uploadToFirebase() {
        if (imageUri!=null){
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("Group_Profile/").child(FirebaseAuth.getInstance().getUid());
            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    final String sdownload_url = String.valueOf(downloadUrl);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("groupIcon", sdownload_url);

                    progressDialog.dismiss();
                    ref.child(groupId).updateChildren(hashMap);

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
}