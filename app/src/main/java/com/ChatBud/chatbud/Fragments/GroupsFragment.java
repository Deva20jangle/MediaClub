package com.ChatBud.chatbud.Fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ChatBud.chatbud.Adapters.GroupChatListAdapter;
import com.ChatBud.chatbud.CreateGroupActivity;
import com.ChatBud.chatbud.Models.GroupChatList;
import com.ChatBud.chatbud.R;
import com.ChatBud.chatbud.SettingActivity;
import com.ChatBud.chatbud.StartActivity;
import com.ChatBud.chatbud.databinding.FragmentGroupsBinding;
import com.ChatBud.chatbud.databinding.FragmentStatusBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupsFragment extends Fragment {

    FragmentGroupsBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    ArrayList<GroupChatList> groupChatLists;
    GroupChatListAdapter groupChatListAdapter;

    String grpId;


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGroupsBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        groupChatLists = new ArrayList<>();

        if (user != null)
        {
            loadGroupChatList();
        }

        return binding.getRoot();
    }

    private void loadGroupChatList() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatLists.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    grpId = ""+ds.child("groupId").getValue();
                    Log.d("TAG", "loadMyGroupRole: groupId in fragment: "+grpId);

                    //if current user's uid exist in participants list then show that group
                    if (ds.child("Participants").child(user.getUid()).exists()){
                        GroupChatList model = ds.getValue(GroupChatList.class);
                        groupChatLists.add(model);
                    }
                }
                groupChatListAdapter = new GroupChatListAdapter(getContext(), groupChatLists );
                binding.groupsRV.setAdapter(groupChatListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onCreate(@Nullable Bundle savedInstanceState){
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.group_main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.settingsGroup:
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
                break;

            case R.id.starMsgGroup:
                Toast.makeText(getContext(), "Starred Messages Group.", Toast.LENGTH_SHORT).show();
                break;

            case R.id.groupChat:
                startActivity(new Intent(getContext(), CreateGroupActivity.class));
                break;
        }

        return true;
    }




}