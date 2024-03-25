package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ChatBud.chatbud.Adapters.ChatsAdapter;
import com.ChatBud.chatbud.Models.Chats;
import com.ChatBud.chatbud.databinding.ActivityChatsBinding;
import com.ChatBud.chatbud.dialog.DialogReviewSendImage;
import com.ChatBud.chatbud.service.FirebaseService;
import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatsActivity extends AppCompatActivity {

    private static final String TAG = "ChatsActivity";
    private static final int REQUEST_CORD_PERMISSION = 332;
    private ActivityChatsBinding binding;
    private ChatsAdapter adapder;
    private final List<Chats> list = new ArrayList<>();
    private String userProfile, userName, bio, phNo, receiverID, FCMToken;
    private String bioFromContact, phNoFromContact;
    private boolean isActionShown = false;
    //private ChatService chatService;

    private int IMAGE_GALLERY_REQUEST = 111;
    private int IMAGE_CAMERA_REQUEST = 300;
    private Uri imageUri = null;

    private final int PICK_AUDIO = 1;
    Uri AudioUri;

    private final int DOCUMENT_REQUEST = 222;

    String senderRoom, receiverRoom;

    FirebaseFirestore firestore;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user;

    //Audio
    private MediaRecorder mediaRecorder;
    private String audio_path, sTime;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.chatToolbar);
        firestore = FirebaseFirestore.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        receiverID = intent.getStringExtra("userID");
        userProfile = intent.getStringExtra("userProfile");
        bio = intent.getStringExtra("Bio");
        phNo = intent.getStringExtra("phoneNo");
        FCMToken = intent.getStringExtra("FCMToken");

        initialize();
        initBtnClick();
        readChats();


        Log.d(TAG, "onCreate: ************************** ");
        Log.d(TAG, "onCreate: SenderRoom " + senderRoom);
        Log.d(TAG, "onCreate: receiverRoom " + receiverRoom);
        Log.d(TAG, "onCreate: ************************** ");

        binding.btnScrollToBottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int lastPosition = list.size() - 1;
                if (lastPosition >= 0){
                    binding.recyclerView.scrollToPosition(lastPosition);
                }
            }
        });

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                Log.d(TAG, "onScrolled: Value:: " + dy);
                if (dy >= 0) {
                    // Scrolling up or at the top
                    binding.btnScrollToBottomButton.setVisibility(View.GONE); // Hide the button
                } else {
                    // Scrolling down
                    binding.btnScrollToBottomButton.setVisibility(View.VISIBLE); // Show the button
                }
            }
        });

    }

    private void initialize() {

        //chatService = new ChatService(this,receiverID);

        if (receiverID != null) {
            Log.d(TAG, "onCreate: receiverID " + receiverID);
            binding.tvUserName.setText(userName);
            if (userProfile != null) {
                if (userProfile.equals("")) {
                    binding.imageProfile.setImageResource(R.drawable.icon_male_ph);  // set  default image when profile user is null
                } else {
                    Glide.with(this).load(userProfile).into(binding.imageProfile);
                }
            }
        }

        binding.edMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(binding.edMessage.getText().toString().trim())) {
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setHasFixedSize(true);

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

    private void readChats() {

        senderRoom = user.getUid() + receiverID;
        receiverRoom = receiverID + user.getUid();

        reference.child("Chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Chats chat = snapshot1.getValue(Chats.class);

                            list.add(chat);
                        }
                        binding.recyclerView.setAdapter(new ChatsAdapter(list, ChatsActivity.this, receiverID));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

/*        chatService.readChatData(new OnReadChatCallBack() {
            @Override
            public void onReadSuccess(List<Chats> list) {
                //adapder.setList(list);
                Log.d(TAG, "onReadSuccess: List "+list.size());
                binding.recyclerView.setAdapter(new ChatsAdapter((ArrayList<Chats>) list,ChatsActivity.this));
            }

            @Override
            public void onReadFailed() {
                Log.d(TAG, "onReadFailed: ");
            }
        });*/
    }

    private void initBtnClick() {

        senderRoom = user.getUid() + receiverID;
        receiverRoom = receiverID + user.getUid();

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(binding.edMessage.getText().toString().trim())) {
                    //chatService.sendTextMsg(binding.edMessage.getText().toString().trim());

                    String message = binding.edMessage.getText().toString().trim();

                    String timestamp = "" + System.currentTimeMillis();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("chatId", "" + timestamp);
                    hashMap.put("dateTime", "" + getCurrentDate());
                    hashMap.put("textMessage", "" + message);
                    hashMap.put("url", "");
                    hashMap.put("senderID", "" + user.getUid());
                    hashMap.put("receiverID", "" + receiverID);
                    hashMap.put("type", "TEXT");

                    reference.child("Chats").child(senderRoom).child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            reference.child("Chats").child(receiverRoom).child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "onCreate: ********* Message Sent *********");
                                    //Add to ChatList
                                    DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(user.getUid()).child(receiverID);
                                    chatRef1.child("chatid").setValue(receiverID);

                                    //
                                    DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(user.getUid());
                                    chatRef2.child("chatid").setValue(user.getUid());
                                }
                            });
                        }
                    });
                    sendNotification(message);
                    binding.edMessage.setText("");
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
                startActivity(new Intent(ChatsActivity.this, UserProfileActivity.class)
                        .putExtra("userID", receiverID)
                        .putExtra("userProfile", userProfile)
                        .putExtra("bio", bio)
                        .putExtra("phNo", phNo)
                        .putExtra("userName", userName));
            }
        });

        binding.btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isActionShown) {
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

    private void sendNotification(String message) {
        Log.d(TAG, "sendNotification: in ChatsActivity FCMToken:: " + FCMToken);

        DocumentReference docRef = firestore.collection("Users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        String userName = document.getString("userName");
                        try {
                            JSONObject jsonObject = new JSONObject();

                            JSONObject notificationObject = new JSONObject();
                            notificationObject.put("title", userName);
                            notificationObject.put("body", message);

                            JSONObject dataObject = new JSONObject();
                            dataObject.put("userID", user.getUid());

                            jsonObject.put("notification", notificationObject);
                            jsonObject.put("data", dataObject);
                            jsonObject.put("to", FCMToken);

                            callAPi(jsonObject);

                        }catch (Exception e){

                        }
                    }
                }
            }
        });

    }

    void callAPi(JSONObject jsonObject){
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAA3RK16uc:APA91bHM_FTDl9fAlctxmGQZgs-_jBQzPfyiLi5yi09oQwJ_siWw_3Gp6YNScn1VkjqZ7y1OKgfA1dSTMhuLpW55-o-G5KWtXHfEWV9sXUHdjs4sGQoI0_6imjpTp5KaQQZ1SrMR8Jiw")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

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
        cv.put(MediaStore.Images.Media.TITLE, "Select Image");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Send Image");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_CAMERA_REQUEST);

        Log.d(TAG, "openCamera: XXXX imageURI in openCamera: " + imageUri);

    }

    public String getCurrentDate() {
        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today = formatter.format(date);

        Calendar currentDateTime = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String currentTime = df.format(currentDateTime.getTime());

        return today + ", " + currentTime;
    }

    private void openGallery() {

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

    private void startRecord() {
        setUpMediaRecorder();

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(ChatsActivity.this, "Recording...", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ChatsActivity.this, "Recording Error , Please restart your app " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void stopRecord() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;

                //sendVoice();
                //chatService.sendVoice(audio_path);

                final Uri uriAudio = Uri.fromFile(new File(audio_path));
                final StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("Chats/Voice/" + System.currentTimeMillis());
                audioRef.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()) ;
                        Uri downloadUrl = urlTask.getResult();
                        String voiceUrl = String.valueOf(downloadUrl);

                        String timestamp = "" + System.currentTimeMillis();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("chatId", "" + timestamp);
                        hashMap.put("dateTime", "" + getCurrentDate());
                        hashMap.put("textMessage", "");
                        hashMap.put("url", "" + voiceUrl);
                        hashMap.put("senderID", "" + user.getUid());
                        hashMap.put("receiverID", "" + receiverID);
                        hashMap.put("type", "VOICE");

                        reference.child("Chats")
                                .child(senderRoom)
                                .child(timestamp)
                                .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                reference.child("Chats")
                                        .child(receiverRoom)
                                        .child(timestamp)
                                        .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //Add to ChatList
                                        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(user.getUid()).child(receiverID);
                                        chatRef1.child("chatid").setValue(receiverID);

                                        //
                                        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(user.getUid());
                                        chatRef2.child("chatid").setValue(user.getUid());
                                    }
                                });
                            }
                        });

                    }
                });

            } else {
                Toast.makeText(getApplicationContext(), "Null", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Stop Recording Error :" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String APP_FOLDER_NAME = "MediaClub";

    private void setUpMediaRecorder() {

        //check for dir
        String app_folder_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_FOLDER_NAME;
        String audio_files_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_FOLDER_NAME + "/" + "Audio";
        File appDirPath = new File(app_folder_path);
        if (!appDirPath.isDirectory()) {
            appDirPath.mkdir();
        }

        File audioDirPath = new File(audio_files_path);
        if (!audioDirPath.isDirectory()) {
            audioDirPath.mkdir();
        }

        String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        String id = user.getPhoneNumber() + timeStamp;

        audio_path = audio_files_path + "/" + id + ".mp3";


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

        Log.d(TAG, "onActivityResult: XXXX imageURI in onActivityResult outside if: " + imageUri);

        if (requestCode == IMAGE_GALLERY_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            imageUri = data.getData();

            Log.d(TAG, "onActivityResult: imageURI from Gallery: " + imageUri);
            Log.d(TAG, "onActivityResult: data from Gallery: " + data);
            Log.d(TAG, "onActivityResult: data.getData from Gallery: " + data.getData());

            //uploadToFirebase();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                reviewImage(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_CAMERA_REQUEST) {

                try {

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    reviewImage(bitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        if (requestCode == PICK_AUDIO && resultCode == RESULT_OK) {

            try {
                // Audio is Picked in format of URI
                AudioUri = data.getData();
                sendAudio(AudioUri);
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        if (requestCode == DOCUMENT_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null){
            uploadDocument(data.getData());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadDocument(Uri data) {

        final ProgressDialog progressDialog = new ProgressDialog(ChatsActivity.this);
        progressDialog.setMessage("Sending document...");
        progressDialog.show();

        //hide action buttons
        binding.layoutActions.setVisibility(View.GONE);
        isActionShown = false;

        String timestamp = "" + System.currentTimeMillis();

        final StorageReference docRef = FirebaseStorage.getInstance().getReference().child("Chats/Documents/" + timestamp);
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
                hashMap.put("chatId", "" + timestamp);
                hashMap.put("dateTime", "" + getCurrentDate());
                hashMap.put("textMessage", fileName);
                hashMap.put("url", "" + voiceUrl);
                hashMap.put("senderID", "" + user.getUid());
                hashMap.put("receiverID", "" + receiverID);
                hashMap.put("type", "DOCUMENT");

                reference.child("Chats")
                        .child(senderRoom)
                        .child(timestamp)
                        .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        reference.child("Chats")
                                .child(receiverRoom)
                                .child(timestamp)
                                .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //Add to ChatList
                                DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(user.getUid()).child(receiverID);
                                chatRef1.child("chatid").setValue(receiverID);

                                //
                                DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(user.getUid());
                                chatRef2.child("chatid").setValue(user.getUid());
                            }
                        });
                    }
                });

                progressDialog.dismiss();

            }
        });

    }

    private void sendAudio(Uri audioUri) {

        Log.d(TAG, "sendAudio: Audio Uri: " + audioUri);

        final ProgressDialog progressDialog = new ProgressDialog(ChatsActivity.this);
        progressDialog.setMessage("Sending audio...");
        progressDialog.show();

        //hide action buttons
        binding.layoutActions.setVisibility(View.GONE);
        isActionShown = false;

        final StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("Chats/Voice/" + System.currentTimeMillis());
        audioRef.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful()) ;
                Uri downloadUrl = urlTask.getResult();
                String voiceUrl = String.valueOf(downloadUrl);

                String timestamp = "" + System.currentTimeMillis();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("chatId", "" + timestamp);
                hashMap.put("dateTime", "" + getCurrentDate());
                hashMap.put("textMessage", "");
                hashMap.put("url", "" + voiceUrl);
                hashMap.put("senderID", "" + user.getUid());
                hashMap.put("receiverID", "" + receiverID);
                hashMap.put("type", "VOICE");

                reference.child("Chats")
                        .child(senderRoom)
                        .child(timestamp)
                        .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        reference.child("Chats")
                                .child(receiverRoom)
                                .child(timestamp)
                                .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //Add to ChatList
                                DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(user.getUid()).child(receiverID);
                                chatRef1.child("chatid").setValue(receiverID);

                                //
                                DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(user.getUid());
                                chatRef2.child("chatid").setValue(user.getUid());
                            }
                        });
                    }
                });

                progressDialog.dismiss();

            }
        });

    }

    private void reviewImage(Bitmap bitmap) {
        new DialogReviewSendImage(ChatsActivity.this, bitmap).show(new DialogReviewSendImage.OnCallBack() {
            @Override
            public void onButtonSendClick() {
                // to Upload Image to firebase storage to get url image...
                if (imageUri != null) {

                    Log.d(TAG, "onActivityResult: imageURI from Gallery: in reviewImage" + imageUri);

                    final ProgressDialog progressDialog = new ProgressDialog(ChatsActivity.this);
                    progressDialog.setMessage("Sending image...");
                    progressDialog.show();

                    //hide action buttons
                    binding.layoutActions.setVisibility(View.GONE);
                    isActionShown = false;

                    new FirebaseService(ChatsActivity.this).uploadImageToFireBaseStorage(imageUri, new FirebaseService.OnCallBack() {
                        @Override
                        public void onUploadSuccess(String imageUrl) {
                            // to send chat image//
                            //chatService.sendImage(imageUrl);
                            String timestamp = "" + System.currentTimeMillis();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("chatId", "" + timestamp);
                            hashMap.put("dateTime", "" + getCurrentDate());
                            hashMap.put("textMessage", "");
                            hashMap.put("url", "" + imageUrl);
                            hashMap.put("senderID", "" + user.getUid());
                            hashMap.put("receiverID", "" + receiverID);
                            hashMap.put("type", "IMAGE");

                            reference.child("Chats")
                                    .child(senderRoom)
                                    .child(timestamp)
                                    .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    reference.child("Chats")
                                            .child(receiverRoom)
                                            .child(timestamp)
                                            .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //Add to ChatList
                                            DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(user.getUid()).child(receiverID);
                                            chatRef1.child("chatid").setValue(receiverID);

                                            //
                                            DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(user.getUid());
                                            chatRef2.child("chatid").setValue(user.getUid());
                                        }
                                    });
                                }
                            });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.phoneCall:
                Toast.makeText(this, "Phone Call", Toast.LENGTH_SHORT).show();
                break;

            case R.id.videoCall:
                Toast.makeText(this, "Video Call", Toast.LENGTH_SHORT).show();
                break;

            case R.id.clearChat:
                Toast.makeText(this, "Clear Chat", Toast.LENGTH_SHORT).show();
                break;

            case R.id.report:
                Toast.makeText(this, "Report", Toast.LENGTH_SHORT).show();
                break;

            case R.id.block:
                Toast.makeText(this, "Block", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}