package com.ChatBud.chatbud.Adapters;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ChatBud.chatbud.ChatsActivity;
import com.ChatBud.chatbud.Models.ChatList;
import com.ChatBud.chatbud.R;
import com.ChatBud.chatbud.dialog.DialogViewUser;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {
    private List<ChatList> list;
    private Context context;

    public ChatListAdapter(List<ChatList> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_chat_list,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        ChatList chatlist = list.get(position);

        holder.tvName.setText(chatlist.getUserName());
        holder.tvDate.setText(chatlist.getDate());

        loadLastMessage(chatlist, holder);

        // for image we need library ...
        if (chatlist.getUrlProfile().equals("")){
            holder.profile.setImageResource(R.drawable.icon_male_ph);  // set  default image when profile user is null
        } else {
            Glide.with(context).load(chatlist.getUrlProfile()).into(holder.profile);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ChatsActivity.class)
                        .putExtra("userID",chatlist.getUserID())
                        .putExtra("userName",chatlist.getUserName())
                        .putExtra("Bio", chatlist.getBio())
                        .putExtra("phoneNo", chatlist.getPhno())
                        .putExtra("FCMToken", chatlist.getFCMToken())
                        .putExtra("userProfile",chatlist.getUrlProfile()));

                Log.d("TAG", "onClick: in chatListAdapter FCMToken:: " + chatlist.getFCMToken());
            }
        });


        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogViewUser(context,chatlist);
            }
        });
    }

    private void loadLastMessage(ChatList chatlist, Holder holder) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Chats")
                .child(auth.getUid() + chatlist.getUserID())
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren())
                        {
                            for (DataSnapshot snapshot1 : snapshot.getChildren())
                            {
                                String type, sender, date;
                                type = ""+snapshot1.child("type").getValue();
                                sender = ""+snapshot1.child("senderID").getValue();
                                date = ""+snapshot1.child("dateTime").getValue();

                                //set date and sender
                                holder.tvDate.setText(date);
                                if (sender.equals(auth.getUid())){
                                    holder.sender.setText("You: ");
                                } else {
                                    holder.sender.setText(chatlist.getUserName()+": ");
                                }
                                //set type of message
                                switch (type) {
                                    case "TEXT":
                                        holder.lstmsg.setText(snapshot1.child("textMessage").getValue().toString());
                                        break;
                                    case "IMAGE":
                                        holder.lstmsg.setText("Image");
                                        break;
                                    case "VOICE":
                                        holder.lstmsg.setText("Voice");
                                        break;
                                    case "DOCUMENT":
                                        holder.lstmsg.setText("Document");
                                        break;
                                }

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
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, lstmsg, tvDate, sender;
        private CircleImageView profile;

        public Holder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_date);
            lstmsg = itemView.findViewById(R.id.lstmsg);
            tvName = itemView.findViewById(R.id.tv_name);
            profile = itemView.findViewById(R.id.image_profile);
            sender = itemView.findViewById(R.id.sender);
        }
    }
}
