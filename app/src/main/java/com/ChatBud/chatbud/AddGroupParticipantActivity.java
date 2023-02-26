package com.ChatBud.chatbud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import com.ChatBud.chatbud.Adapters.AddParticipantAdapter;
import com.ChatBud.chatbud.Adapters.ContactsAdapter;
import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.databinding.ActivityAddGroupParticipantBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class AddGroupParticipantActivity extends AppCompatActivity {

    ActivityAddGroupParticipantBinding binding;
    private static final String TAG = "AddGroupParticipantActivity";
    public static final int REQUEST_READ_CONTACTS = 79;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;
    ActionBar actionBar;

    String groupId, myGroupRole;

    private ArrayList<Users> usersList = new ArrayList<>();
    private AddParticipantAdapter addParticipantAdapter;

    private ArrayList mobileArray, contactArray;
    ArrayList<String> phoneList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddGroupParticipantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        groupId = getIntent().getStringExtra("groupId");

        loadGroupInfo();

//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
//        reference.child(groupId).child("Participants").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()){
//                    myGroupRole = ""+snapshot.child("role").getValue();
//                    //actionBar.setTitle(groupTitle + "("+myGroupRole+")");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        Log.d("TAG", "onDataChange: groupID in adapter: "+groupId);
        Log.d("TAG", "onDataChange: MyGroupRole in activity: "+myGroupRole);

        getAllUsers();

    }

    private void getAllUsers() {

        if (firebaseUser != null) {
            getContactFromPhone(); // If they using this app
        }

        if (mobileArray != null) {
            getContactList();

        }
    }

    private void getContactFromPhone() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            mobileArray = getAllPhoneContacts();
        } else {
            requestPermission();
        }

    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
            // show UI part if you want here to show some rationale !!!
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mobileArray = getAllPhoneContacts();
                    for (int i = 0; i <= mobileArray.size(); i++) {
                        Log.d("TAG", "onRequestPermissionsResult: Mobile Array:: " + mobileArray.get(i));
                    }
                } else {
                    finish();
                }
                return;
            }
        }
    }

    private ArrayList getAllPhoneContacts() {

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
//                String name = cur.getString(cur.getColumnIndex(
//                        ContactsContract.Contacts.DISPLAY_NAME));
//                nameList.add(name);

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneList.add(phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }

        return phoneList;
    }

    private void getContactList() {

        firestore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                    String userID = snapshots.getString("userID");
                    String userName = snapshots.getString("userName");
                    String imageUrl = snapshots.getString("imageProfile");
                    String desc = snapshots.getString("bio");
                    String phone = snapshots.getString("userPhone");

                    Users user = new Users();
                    user.setUserID(userID);
                    user.setBio(desc);
                    user.setUserName(userName);
                    user.setImageProfile(imageUrl);
                    user.setUserPhone(phone);

                    String  dbpno;

                    if (userID != null && !userID.equals(firebaseUser.getUid())){
                        Log.d("TAG", "onSuccess: UserPhone ***************************************::" );
                        Log.d("TAG", "onSuccess: UserPhone Outside if ::"+user.getUserPhone());

                        dbpno = user.getUserPhone();
                        dbpno = dbpno.replace(" ","");

                        if (mobileArray.contains(user.getUserPhone()) || mobileArray.contains(dbpno)){
                            Log.d("TAG", "onSuccess: UserPhone inside if::" +user.getUserPhone());
                            usersList.add(user);
                        }else if (user.getUserPhone().contains("+91")){
                            String cno1 = user.getUserPhone().substring(4, user.getUserPhone().length()).toString();
                            Log.d("TAG", "onSuccess: UserPhone Cno::"+cno1);
                            if (mobileArray.contains(cno1) || mobileArray.contains(dbpno)){
                                Log.d("TAG", "onSuccess: UserPhone substring::" + cno1);
                                usersList.add(user);
                            }
                        }

                    }
                }

                addParticipantAdapter = new AddParticipantAdapter(AddGroupParticipantActivity.this, usersList, ""+groupId, ""+myGroupRole);
                binding.usersRv.setAdapter(addParticipantAdapter);
            }

        });


    }

    private void loadGroupInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");

        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String groupDesc = ""+ds.child("groupDescription").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    //actionBar.setTitle("Add Participants");

                    ref1.child(groupId).child("Participants").child(auth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        myGroupRole = ""+snapshot.child("role").getValue();
                                        //actionBar.setTitle(groupTitle + "("+myGroupRole+")");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}