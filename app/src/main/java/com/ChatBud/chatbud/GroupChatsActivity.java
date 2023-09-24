package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ChatBud.chatbud.Adapters.GroupChatsAdapter;
import com.ChatBud.chatbud.Models.Chats;
import com.ChatBud.chatbud.Models.GroupChats;
import com.ChatBud.chatbud.databinding.ActivityGroupChatsBinding;
import com.ChatBud.chatbud.dialog.DialogReviewSendImage;
import com.ChatBud.chatbud.service.ChatService;
import com.ChatBud.chatbud.service.FirebaseService;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GroupChatsActivity extends AppCompatActivity {

    ActivityGroupChatsBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestore;

    private static final String TAG = "GroupChatsActivity";
    //ChatService chatService;

    private int IMAGE_GALLERY_REQUEST = 111;
    private int IMAGE_CAMERA_REQUEST = 300;

    private static final int REQUEST_CORD_PERMISSION = 332;
    private boolean isActionShown = false;
    private Uri imageUri = null;

    private final int PICK_AUDIO = 1;
    Uri AudioUri;

    private final int DOCUMENT_REQUEST = 222;

    private GroupChatsAdapter groupChatsAdapter;
    private ArrayList<GroupChats> groupChats;

    //Audio
    private MediaRecorder mediaRecorder;
    private String audio_path;
    private String sName, sTime;

    public String groupId, groupTitle, myGroupRole="", myRole;

    String pinnedMessage, pinnedSender, pinnedURL, pinnedType, pinnedMessageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        groupTitle = intent.getStringExtra("groupTitle");

        Log.d("TAG", "onCreate: OnLongClickListener:: groupID:: " + groupId);

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.chatToolbar);

        loadGroupInfo();
        initButtonClick();
        loadGroupMessages();
        loadMyGroupRole();

        //myGrpRole(groupId, user.getUid());

        Log.d("TAG", "onCreate: LongClickListener:: Role: "+myGroupRole);

        if (user != null){
            firestore.collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    sName = Objects.requireNonNull(documentSnapshot.get("userName")).toString();
                    Log.d("sender", "sName : " + sName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GroupChatsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        binding.chatToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(GroupChatsActivity.this, GroupInfoActivity.class);
                intent1.putExtra("groupId", groupId);
                startActivity(intent1);
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("PinnedMessage");
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pinnedMessage = snapshot.child("Message").getValue(String.class);
                pinnedSender = snapshot.child("SenderName").getValue(String.class);
                pinnedURL = snapshot.child("url").getValue(String.class);
                pinnedType = snapshot.child("Type").getValue(String.class);

                if (pinnedMessage != null && pinnedSender != null){
                    binding.lnPinMessage.setVisibility(View.VISIBLE);
                    binding.txtPinMessage.setText(pinnedMessage);
                    binding.txtPinSender.setText(pinnedSender);

                    if (!pinnedURL.equals("")){

                        binding.txtPinURL.setVisibility(View.VISIBLE);

                        if (pinnedType.equals("IMAGE") && pinnedMessage.equals("")){
                            binding.txtPinMessage.setText("IMAGE");
                        } else if (pinnedType.equals("VOICE") && pinnedMessage.equals("")){
                            binding.txtPinMessage.setText("VOICE");
                        } else if(pinnedType.equals("DOCUMENT") && pinnedMessage.equals("")){
                            binding.txtPinMessage.setText("DOCUMENT");
                        }

                        binding.txtPinURL.setText(pinnedURL);

                    } else {
                        binding.txtPinURL.setVisibility(View.GONE);
                    }

                } else {
                    binding.lnPinMessage.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.lnPinMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("PinnedMessage");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pinnedMessageId = snapshot.child("MessageID").getValue(String.class);
                        Log.d(TAG, "onDataChange: Pinned MessageId :: " + pinnedMessageId);
                        int pinnedMessagePosition = groupChatsAdapter.getPositionByMessageId(pinnedMessageId);
                        if (pinnedMessagePosition != -1){
                            binding.recyclerView.smoothScrollToPosition(pinnedMessagePosition);
                        } else {
                            Toast.makeText(GroupChatsActivity.this, "Pinned message has been deleted.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    private void loadMyGroupRole() {

        Log.d(TAG, "loadMyGroupRole: LongClickListener:: groupID:" + groupId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //myGroupRole = ""+snapshot.child("role").getValue();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            //refresh menu item
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadGroupMessages() {
        groupChats = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChats.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    GroupChats model = ds.getValue(GroupChats.class);
                    groupChats.add(model);
                }

                //set to adapter and recyclerview
                groupChatsAdapter = new GroupChatsAdapter(GroupChatsActivity.this, groupChats, groupId);
                binding.recyclerView.setAdapter(groupChatsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initButtonClick() {

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(binding.edMessage.getText().toString().trim())){
                    //Send Message
                    String message = binding.edMessage.getText().toString().trim();
                    //timestamp
                    String timestamp = ""+System.currentTimeMillis();

                    //setup message data
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("messageID", ""+timestamp);
                    hashMap.put("sender", ""+user.getUid());
                    hashMap.put("senderName", ""+sName);
                    hashMap.put("message", ""+message);
                    hashMap.put("timestamp", ""+timestamp);
                    hashMap.put("url", "");
                    hashMap.put("type", "TEXT");

                    //add in db
                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                    ref1.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //message sent
                            //clear message editText
                            binding.edMessage.setText("");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to send message
                            Toast.makeText(GroupChatsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.lnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatsActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        binding.btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isActionShown){
                    binding.layoutActions.setVisibility(View.GONE);
                    isActionShown = false;
                } else {
                    binding.layoutActions.setVisibility(View.VISIBLE);
                    isActionShown = true;
                }

            }
        });

        binding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        binding.btnCameraX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermissions();
            }
        });

        binding.btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectAudio();
            }
        });

        binding.btnDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDocument();
            }
        });
    }

    private void selectDocument() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*");
        startActivityForResult(intent, DOCUMENT_REQUEST);

    }

    private void selectAudio() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO);

    }

    private void checkCameraPermissions() {

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

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Select Image");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Send Image");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_CAMERA_REQUEST);

        Log.d(TAG, "openCamera: XXXX imageURI in openCamera: " + imageUri);

    }

    private void openGallery(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);

    }

    private boolean checkPermissionFromDevice() {
        int write_external_strorage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_strorage_result == PackageManager.PERMISSION_DENIED || record_audio_result == PackageManager.PERMISSION_DENIED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_CORD_PERMISSION);
    }

    private void startRecord(){
        setUpMediaRecorder();

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(GroupChatsActivity.this, "Recording...", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(GroupChatsActivity.this, "Recording Error , Please restart your app "+e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void stopRecord(){
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;

                //sendVoice();
                sendVoice(audio_path);

            } else {
                Toast.makeText(getApplicationContext(), "Null", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Stop Recording Error :" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void sendVoice(String audioPath){
        final Uri uriAudio = Uri.fromFile(new File(audioPath));
        final StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("GroupChats/Voice/" + System.currentTimeMillis());
        audioRef.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot audioSnapshot) {
                Task<Uri> urlTask = audioSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful()) ;
                Uri downloadUrl = urlTask.getResult();
                String voiceUrl = String.valueOf(downloadUrl);

                String timestamp = ""+System.currentTimeMillis();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("messageID", ""+timestamp);
                hashMap.put("sender", ""+user.getUid());
                hashMap.put("senderName", ""+sName);
                hashMap.put("message", "");
                hashMap.put("url", "" + voiceUrl);
                hashMap.put("timestamp", ""+timestamp);
                hashMap.put("type", "VOICE");

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                ref1.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Send", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to send message
                        Toast.makeText(GroupChatsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private static String APP_FOLDER_NAME = "MediaClub";
    private void setUpMediaRecorder() {
        //check for dir
        String app_folder_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_FOLDER_NAME;
        String audio_files_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_FOLDER_NAME + "/" + "Audio";
        File appDirPath = new File(app_folder_path);
        if (!appDirPath.isDirectory()){
            appDirPath.mkdir();
        }

        File audioDirPath = new File(audio_files_path);
        if (!audioDirPath.isDirectory()){
            audioDirPath.mkdir();
        }

        String id = user.getUid() + sTime;

        audio_path = audio_files_path + "/" + id + "audio_record.mp3";

        mediaRecorder = new MediaRecorder();
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audio_path);
        } catch (Exception e) {
            Log.d(TAG, "setUpMediaRecord: " + e.getMessage());
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_GALLERY_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null){

            imageUri = data.getData();

            //uploadToFirebase();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                reviewImage(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_CAMERA_REQUEST){
                try {

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    reviewImage(bitmap);

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == PICK_AUDIO && resultCode == RESULT_OK) {
            // Audio is Picked in format of URI
            AudioUri = data.getData();
            sendAudio(AudioUri);
        }

        if (requestCode == DOCUMENT_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null){
            uploadDocument(data.getData());
        }

    }

    private void uploadDocument(Uri data) {

        final ProgressDialog progressDialog = new ProgressDialog(GroupChatsActivity.this);
        progressDialog.setMessage("Sending document...");
        progressDialog.show();

        //hide action buttons
        binding.layoutActions.setVisibility(View.GONE);
        isActionShown = false;

        String timestamp = "" + System.currentTimeMillis();

        final StorageReference docRef = FirebaseStorage.getInstance().getReference().child("GroupChats/Documents/" + timestamp);
        docRef.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful()) ;
                Uri downloadUrl = urlTask.getResult();
                String voiceUrl = String.valueOf(downloadUrl);

                Cursor cursor = getContentResolver().query(data, null, null, null);
                if (cursor.getCount() <= 0){
                    cursor.close();
                }

                cursor.moveToFirst();
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                cursor.close();

                Log.d(TAG, "onSuccess: FileName: " + fileName);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("messageID", ""+timestamp);
                hashMap.put("sender", ""+user.getUid());
                hashMap.put("senderName", ""+sName);
                hashMap.put("message", ""+fileName);
                hashMap.put("timestamp", ""+timestamp);
                hashMap.put("url", ""+voiceUrl);
                hashMap.put("type", "DOCUMENT");

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                ref1.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Send", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to send message
                        Toast.makeText(GroupChatsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                progressDialog.dismiss();
            }
        });

    }

    private void sendAudio(Uri audioUri) {

        Log.d(TAG, "sendAudio: Audio Uri: " + audioUri);

        final ProgressDialog progressDialog = new ProgressDialog(GroupChatsActivity.this);
        progressDialog.setMessage("Sending audio...");
        progressDialog.show();

        //hide action buttons
        binding.layoutActions.setVisibility(View.GONE);
        isActionShown = false;

        final StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("GroupChats/Voice/" + System.currentTimeMillis());
        audioRef.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot audioSnapshot) {
                Task<Uri> urlTask = audioSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful()) ;
                Uri downloadUrl = urlTask.getResult();
                String voiceUrl = String.valueOf(downloadUrl);

                String timestamp = ""+System.currentTimeMillis();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("messageID", ""+timestamp);
                hashMap.put("sender", ""+user.getUid());
                hashMap.put("senderName", ""+sName);
                hashMap.put("message", "");
                hashMap.put("timestamp", ""+timestamp);
                hashMap.put("url", ""+voiceUrl);
                hashMap.put("type", "VOICE");

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                ref1.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Send", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to send message
                        Toast.makeText(GroupChatsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                progressDialog.dismiss();

            }
        });

    }

    private void reviewImage(Bitmap bitmap){
        new DialogReviewSendImage(GroupChatsActivity.this,bitmap).show(new DialogReviewSendImage.OnCallBack() {
            @Override
            public void onButtonSendClick() {
                // to Upload Image to firebase storage to get url image...
                if (imageUri!=null){
                    final ProgressDialog progressDialog = new ProgressDialog(GroupChatsActivity.this);
                    progressDialog.setMessage("Sending image...");
                    progressDialog.show();

                    //hide action buttonss
                    binding.layoutActions.setVisibility(View.GONE);
                    isActionShown = false;

                    new FirebaseService(GroupChatsActivity.this).uploadImageToFireBaseStorageGroup(imageUri, new FirebaseService.OnCallBack() {
                        @Override
                        public void onUploadSuccess(String imageUrl) {
                            // to send chat image//
                            sendImage(imageUrl);
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onUploadFailed(Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

            }
        });
    }

    public void sendImage(String imageUrl){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("messageID", ""+timestamp);
        hashMap.put("sender", ""+user.getUid());
        hashMap.put("senderName", ""+sName);
        hashMap.put("message", "");
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("url", ""+imageUrl);
        hashMap.put("type", "IMAGE");

        //add url in db
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
        ref1.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Send", "onSuccess: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to send message
                Toast.makeText(GroupChatsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadGroupInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds: snapshot.getChildren()){
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDesc = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();

                    binding.txtGroupTitle.setText(groupTitle);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.group_icon).into(binding.groupIcon);
                    } catch (Exception e) {
                        binding.groupIcon.setImageResource(R.drawable.group_icon);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.edMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(binding.edMessage.getText().toString().trim())){
                    binding.btnSend.setVisibility(View.INVISIBLE);
                    binding.recordButton.setVisibility(View.VISIBLE);
                } else {
                    binding.btnSend.setVisibility(View.VISIBLE);
                    binding.recordButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL,false);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setHasFixedSize(true);
        //adapder = new ChatsAdapder(list,this);
        //binding.recyclerView.setAdapter(new ChatsAdapder(list,this));

        //initialize record button
        binding.recordButton.setRecordView(binding.recordView);
        binding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {

                //Start Recording..
                if (!checkPermissionFromDevice()) {
                    binding.btnEmoji.setVisibility(View.INVISIBLE);
                    binding.btnFile.setVisibility(View.INVISIBLE);
                    binding.edMessage.setVisibility(View.INVISIBLE);

                    startRecord();
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(100);
                    }

                } else {
                    requestPermission();
                }

            }

            @Override
            public void onCancel() {
                try {
                    mediaRecorder.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(long recordTime) {
                binding.btnEmoji.setVisibility(View.VISIBLE);
                binding.btnFile.setVisibility(View.VISIBLE);
                binding.edMessage.setVisibility(View.VISIBLE);

                //Stop Recording..
                try {
                    sTime = getHumanTimeText(recordTime);
                    stopRecord();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLessThanSecond() {
                binding.btnEmoji.setVisibility(View.VISIBLE);
                binding.btnFile.setVisibility(View.VISIBLE);
                binding.edMessage.setVisibility(View.VISIBLE);
            }
        });
        binding.recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                binding.btnEmoji.setVisibility(View.VISIBLE);
                binding.btnFile.setVisibility(View.VISIBLE);
                binding.edMessage.setVisibility(View.VISIBLE);
            }
        });

    }

    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d",
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_menu, menu);

        menu.findItem(R.id.add_participant).setVisible(false);
        menu.findItem(R.id.delete_group).setVisible(false);

        if (myGroupRole.equals("creator") || myGroupRole.equals("admin")){
            menu.findItem(R.id.add_participant).setVisible(true);
        }

        if (myGroupRole.equals("creator")){
            menu.findItem(R.id.leave_group).setVisible(false);
            menu.findItem(R.id.delete_group).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId())
        {
            case R.id.groupInfo:
                Toast.makeText(GroupChatsActivity.this, "Group Info.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GroupChatsActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
                break;

            case R.id.add_participant:
                Toast.makeText(this, "Add Participant", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(GroupChatsActivity.this, AddGroupParticipantActivity.class);
                intent1.putExtra("groupId", groupId);
                startActivity(intent1);
                break;

            case R.id.leave_group:
                new AlertDialog.Builder(GroupChatsActivity.this).setTitle("Leave Group").setMessage("Are you sure that you want to leave the group.").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Participants").child(user.getUid()).setValue(null);
                            Intent intent = new Intent(GroupChatsActivity.this, MainActivity.class);
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
                break;

            case R.id.delete_group:
                new AlertDialog.Builder(GroupChatsActivity.this).setTitle("Delete Group")
                        .setMessage("Deleting the group will delete all the messages and media sent in the group chat \n\nAre you sure that you want to delete the group.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                                    ref.child(groupId).setValue(null);
                                    Intent intent = new Intent(GroupChatsActivity.this, MainActivity.class);
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
                break;
        }

        return true;
    }
}