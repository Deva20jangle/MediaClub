package com.ChatBud.chatbud.Fragments;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ChatBud.chatbud.Adapters.ChatListAdapter;
import com.ChatBud.chatbud.ContactsActivity;
import com.ChatBud.chatbud.CreateGroupActivity;
import com.ChatBud.chatbud.MainActivity;
import com.ChatBud.chatbud.Models.ChatList;
import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.ProfileActivity;
import com.ChatBud.chatbud.R;
import com.ChatBud.chatbud.SettingActivity;
import com.ChatBud.chatbud.StartActivity;
import com.ChatBud.chatbud.databinding.FragmentChatsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Objects;


public class ChatsFragment extends Fragment {

    static final String TAG = "ChatsFragment";


    public ChatsFragment() {
        // Required empty public constructor
    }

    FragmentChatsBinding binding;
    ArrayList<ChatList> list;
    FirebaseDatabase database;
    FirebaseUser firebaseUser;
    FirebaseAuth auth;
    DatabaseReference reference;
    FirebaseFirestore firestore;
    Handler handler = new Handler();

    ProgressDialog pd;

    String DeviceID;
    CountDownTimer countDownTimer;

    private ArrayList<String> allUserID;

    ChatListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        database = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        pd = new ProgressDialog(getContext());

        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;
        DeviceID = activity.myDeviceID;

        Log.d(TAG, "onCreateView: DeviceID in fragment:: " + DeviceID);

        list = new ArrayList<>();
        allUserID = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.chatrecyclerView.setLayoutManager(layoutManager);
        adapter = new ChatListAdapter(list, getContext());
        binding.chatrecyclerView.setAdapter(adapter);

        if (firebaseUser != null)
        {
//            getLoginStatus();
            getChat();
        }

        return binding.getRoot();

//        binding.fabAction.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), ContactsActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    private void getLoginStatus() {

        countDownTimer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                countDownTimer.start();
                firestore.collection("Users")
                        .document(firebaseUser.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String deviceID = Objects.requireNonNull(documentSnapshot.get("deviceID")).toString();
                                if (!deviceID.equals(DeviceID)) {

                                    Log.d("TAG", "onSuccess: Fragment DeviceID database :: " + deviceID);
                                    Log.d("TAG", "onSuccess: Fragment DeviceID Phone :: " + DeviceID);

                                    //auth.signOut();

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Logging out");
                                    builder.setMessage("Your account is logged in another device. If it is not you then contact to us");
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            auth.signOut();
                                            dialog.cancel();
                                            startActivity(new Intent(getContext(), StartActivity.class));
                                        }
                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error in single device login" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "onFailure: Error in single device login" + e.getMessage());
                    }
                });
            }
        }.start();

    }

    private void getChat() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        reference.child("ChatList").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                allUserID.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    String userID = Objects.requireNonNull(snapshot.child("chatid").getValue()).toString();
                    Log.d("TAG", "userID: "+userID);

                    allUserID.add(userID);

                }
                binding.progressCircular.setVisibility(View.GONE);
                getUserinfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserinfo(){

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (String userID: allUserID){
                    firestore.collection("Users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            try {
                                ChatList chatList = new ChatList(
                                        documentSnapshot.getString("userID"),
                                        documentSnapshot.getString("userName"),
                                        "",
                                        documentSnapshot.getString("imageProfile"),
                                        documentSnapshot.getString("bio"),
                                        documentSnapshot.getString("userPhone"),
                                        documentSnapshot.getString("FCMToken")
                                );
                                list.add(chatList);
                            }catch (Exception e){
                                Log.d(TAG, "onSuccess: "+e.getMessage());
                            }
                            if (adapter != null){
                                binding.lnInvite.setVisibility(View.GONE);
                                adapter.notifyItemInserted(0);
                                adapter.notifyDataSetChanged();
                            }

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Error"+e.getMessage());
                        }
                    });
                }

            }
        });
    }

    public void onCreate(@Nullable Bundle savedInstanceState){
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.settingsChat:
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
                break;

            case R.id.brdCastChat:
                Toast.makeText(getContext(), "New Broadcast.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.starMsgChat:
                Toast.makeText(getContext(), "Starred Messages Chats.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.logout:

                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Log Out");
                builder.setMessage("Do you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                firestore.collection("Users").document(firebaseUser.getUid()).update("FCMToken", "").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getContext(), "logged out successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                dialog.cancel();
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(getContext(), StartActivity.class));
                            }
                        });
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

//                auth.signOut();
//                Intent i = new Intent(MainActivity.this, StartActivity.class);
//                startActivity(i);
//                finish();
//                break;
        return true;
    }


}