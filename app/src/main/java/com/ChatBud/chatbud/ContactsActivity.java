package com.ChatBud.chatbud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.ChatBud.chatbud.Adapters.ContactsAdapter;
import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.databinding.ActivityContactsBinding;
import com.ChatBud.chatbud.interfaces.OnContactClickListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsActivity";
    private ActivityContactsBinding binding;
    private List<Users> list = new ArrayList<>();
    private ContactsAdapter adapter;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    public static final int REQUEST_READ_CONTACTS = 79;
    private ListView contactlist;
    private ArrayList mobileArray, contactArray;
    ArrayList<String> phoneList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            getContactFromPhone(); // If they using this app
            // getContactList();
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
                        Log.d(TAG, "onRequestPermissionsResult: Mobile Array:: " + mobileArray.get(i));
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

//                    if (userID != null && !userID.equals(firebaseUser.getUid())) {
//                        if (mobileArray.contains(user.getUserPhone())){
//                            list.add(user);
//                        }
//                    }

                    String  dbpno;

                    if (userID != null && !userID.equals(firebaseUser.getUid())){
                        Log.d(TAG, "onSuccess: UserPhone ***************************************::" );
                        Log.d(TAG, "onSuccess: UserPhone Outside if ::"+user.getUserPhone());
                        
//                        for (int i=0; i<=mobileArray.size(); i++){
//                            dbpno = mobileArray.get(i).toString();
//                            dbpno = dbpno.replace(" ", "");
//
//                            contactArray.add(dbpno);
//                        }

                        dbpno = user.getUserPhone();
                        dbpno = dbpno.replace(" ","");

                        if (mobileArray.contains(user.getUserPhone()) || mobileArray.contains(dbpno)){
                            Log.d(TAG, "onSuccess: UserPhone inside if::" +user.getUserPhone());
                            list.add(user);
                        }else if (user.getUserPhone().contains("+91")){
                            String cno1 = user.getUserPhone().substring(4, user.getUserPhone().length()).toString();
                            Log.d(TAG, "onSuccess: UserPhone Cno::"+cno1);
                            if (mobileArray.contains(cno1) || mobileArray.contains(dbpno)){
                                Log.d(TAG, "onSuccess: UserPhone substring::" + cno1);
                                list.add(user);
                            }
                        }

                    }
                }
//                for (Users user : list){
//                    if (mobileArray.contains(user.getUserPhone())){
//                        Log.d(TAG, "onSuccess: ------------------------------------------------------------------------------------------");
//                        Log.d(TAG, "onSuccess getContactList: true "+user.getUserPhone() );
//                        Log.d(TAG, "onSuccess: ------------------------------------------------------------------------------------------");
//                    } else {
//                        Log.d(TAG, "onSuccess: ------------------------------------------------------------------------------------------");
//                        Log.d(TAG, "onSuccess getContactList: false"+user.getUserPhone());
//                        Log.d(TAG, "onSuccess: ------------------------------------------------------------------------------------------");
//                    }
//                }
                adapter = new ContactsAdapter(list, ContactsActivity.this);
                binding.recyclerView.setAdapter(adapter);
            }

        });


    }

}