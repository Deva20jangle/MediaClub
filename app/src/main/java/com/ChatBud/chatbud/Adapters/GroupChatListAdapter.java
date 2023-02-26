package com.ChatBud.chatbud.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ChatBud.chatbud.GroupChatsActivity;
import com.ChatBud.chatbud.Models.GroupChatList;
import com.ChatBud.chatbud.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatListAdapter extends RecyclerView.Adapter<GroupChatListAdapter.HolderGroupChatList> {

    private Context context;
    private ArrayList<GroupChatList> groupChatLists;
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");

    public GroupChatListAdapter(Context context, ArrayList<GroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.layout_groupchat_list, parent, false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatListAdapter.HolderGroupChatList holder, int position) {

        //get data
        GroupChatList model = groupChatLists.get(position);

        String groupId = model.getGroupId();
        String groupTitle = model.getGroupTitle();
        String groupIcon = model.getGroupIcon();

        //get last message from group
        loadLastMessage(model, holder);

        //set data
        holder.tvGroupTitle.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.group_icon).into(holder.groupIcon);
        } catch (Exception e) {
            holder.groupIcon.setImageResource(R.drawable.group_icon);
        }

        //handle group click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open group chat
                Intent intent = new Intent(context, GroupChatsActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("groupTitle", groupTitle);
                context.startActivity(intent);
            }
        });

    }

    private void loadLastMessage(GroupChatList model, HolderGroupChatList holder) {
        ref.child(model.getGroupId())
                .child("Messages")
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            //get dataMessages
                            String message = "" + ds.child("message").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String sName = "" + ds.child("senderName").getValue();
                            String type = "" + ds.child("type").getValue();

                            //convert time
                            Calendar cal = Calendar.getInstance(Locale.getDefault());
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            DateFormat df = new SimpleDateFormat("dd-MM-yyyy, hh:mm a", Locale.getDefault());
                            String dateTime = df.format(Long.parseLong(timestamp)).toString();

                            Log.d("TAG", "onDataChange: Date:"+dateTime);

                            //set Data
                            holder.tvLastMsg.setText(message);
                            holder.tvSender.setText(sName);

                            //set data to holder
                            switch (type) {
                                case "TEXT":
                                    holder.tvLastMsg.setText(message);
                                    holder.tvSender.setText(sName);
                                    holder.tvTime.setText(dateTime);
                                    break;
                                case "IMAGE":
                                    holder.tvLastMsg.setText("IMAGE");
                                    holder.tvSender.setText(sName);
                                    holder.tvTime.setText(dateTime);
                                    break;
                                case "VOICE":
                                    holder.tvLastMsg.setText("VOICE");
                                    holder.tvSender.setText(sName);
                                    holder.tvTime.setText(dateTime);
                                    break;
                                case "DOCUMENT":
                                    holder.tvLastMsg.setText("Document");
                                    holder.tvSender.setText(sName);
                                    holder.tvTime.setText(dateTime);
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }


    //view Holder class
    class HolderGroupChatList extends RecyclerView.ViewHolder {

        private CircleImageView groupIcon;
        private TextView tvGroupTitle, tvSender, tvLastMsg, tvTime;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIcon = itemView.findViewById(R.id.groupIcon);
            tvGroupTitle = itemView.findViewById(R.id.tvGroupTitle);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvLastMsg = itemView.findViewById(R.id.tvLastMsg);
            tvTime = itemView.findViewById(R.id.tvTime);

        }
    }

}
