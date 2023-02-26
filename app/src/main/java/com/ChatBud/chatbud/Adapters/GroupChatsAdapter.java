package com.ChatBud.chatbud.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ChatBud.chatbud.GroupChatsActivity;
import com.ChatBud.chatbud.Models.Chats;
import com.ChatBud.chatbud.Models.GroupChats;
import com.ChatBud.chatbud.R;
import com.ChatBud.chatbud.ReviewImageActivity;
import com.ChatBud.chatbud.service.AudioService;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GroupChatsAdapter extends RecyclerView.Adapter<GroupChatsAdapter.HolderGroupChat> {

    private ArrayList<GroupChats> groupChats;
    private Context context;
    private String groupID;

    public static final int MSG_TYPE_left = 0;
    public static final int MSG_TYPE_right = 1;

    private TextView textMessage, txtSenderName, txtImageSenderName, txtAudioSenderName, txtDocSenderName, txtFileName;
    private LinearLayout layoutText, layoutImage, layoutVoice, layoutDoc;
    private ImageView imageMessage;
    private MediaPlayer tmpMediaPlayer;
    String sName;
    String role, messageSender;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();


    public GroupChatsAdapter(Context context, ArrayList<GroupChats> groupChats, String groupID) {
        this.context = context;
        this.groupChats = groupChats;
        this.groupID = groupID;
    }

    public void setList(ArrayList<GroupChats> groupChats) {
        this.groupChats = groupChats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layouts
        if (viewType == MSG_TYPE_left) {
            View view = LayoutInflater.from(context).inflate(R.layout.groupchat_item_left, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.groupchat_item_right, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        holder.bind(groupChats.get(position));

        GroupChats groupChat = groupChats.get(position);

        if (user != null){
            firestore.collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    sName = Objects.requireNonNull(documentSnapshot.get("userName")).toString();
                    Log.d("sender", "Message Sender:: sName : " + sName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            ref.child("Groups").child(groupID).child("Participants").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    role = "" + snapshot.child("role").getValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            Log.d("TAG", "onLongClick: role outside LongClickListener:: " + role);

        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Log.d("TAG", "onLongClick: role:: " + role);

                String[] options;

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Select");
                options = new String[]{"Pin Message", "Delete Message"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){

                            if (role.equals("creator") || role.equals("admin")){

                                String pinnedSender = groupChat.getSender();
                                String pinnedSenderName = groupChat.getSenderName();
                                String pinnedMessageID = groupChat.getMessageID();
                                String pinnedMessage = groupChat.getMessage();
                                String pinnedMessageType = groupChat.getType();
                                String pinnedURL = groupChat.getUrl();

                                HashMap<String, Object> hashMap = new HashMap<>();

                                if (pinnedMessageType.equals("TEXT")){
                                    hashMap.put("Sender", pinnedSender);
                                    hashMap.put("SenderName", pinnedSenderName);
                                    hashMap.put("MessageID", pinnedMessageID);
                                    hashMap.put("Message", pinnedMessage);
                                    hashMap.put("Type", pinnedMessageType);
                                    hashMap.put("url", "");
                                } else {
                                    hashMap.put("Sender", pinnedSender);
                                    hashMap.put("SenderName", pinnedSenderName);
                                    hashMap.put("MessageID", pinnedMessageID);
                                    hashMap.put("Message", pinnedMessage);
                                    hashMap.put("Type", pinnedMessageType);
                                    hashMap.put("url", pinnedURL);
                                }

                                ref.child("Groups").child(groupID).child("PinnedMessage").setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Message Pinned", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to pin message. Please try again letter.", Toast.LENGTH_SHORT).show();
                                        Log.d("TAG", "onFailure: Failed to pin message." + e.getMessage());
                                    }
                                });

                            } else {
                                Toast.makeText(context, "You are not admin or  creator. You can't pin the message.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            messageSender = groupChat.getSender();
                            Log.d("TAG", "onClick: Message Sender:: " + messageSender);
                            //Toast.makeText(context, "Name: " + user.getUid(), Toast.LENGTH_SHORT).show();

                            if (messageSender.equals(user.getUid())){
                                try {
                                    ref.child("Groups").child(groupID).child("Messages").child(groupChat.getMessageID()).setValue(null);
                                } catch (Exception e){
                                    Log.d("TAG", "onClick: Something went wrong please try again." + e.getMessage());
                                    Toast.makeText(context, "Something went wrong please try again.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                                dialog.setTitle("Delete");
                                dialog.setMessage("You are not sender of this message. So you can not  delete this message.");
                                dialog.setCancelable(false);
                                dialog.setPositiveButton("OK", null);
                                dialog.show();
                            }

                        }
                    }
                }).show();

                return false;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupChat.getType().equals("IMAGE")){
                    String image = groupChat.getMessage();
                    Intent intent = new Intent(context, ReviewImageActivity.class);
                    intent.putExtra("image", image);
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return groupChats.size();
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        ImageView btnPlay;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            textMessage = itemView.findViewById(R.id.txtMessage);
            txtFileName = itemView.findViewById(R.id.txtFileName);
            txtSenderName = itemView.findViewById(R.id.txtSenderName);
            txtImageSenderName = itemView.findViewById(R.id.txtImageSenderName);
            txtAudioSenderName = itemView.findViewById(R.id.txtAudioSenderName);
            txtDocSenderName = itemView.findViewById(R.id.txtDocSenderName);
            layoutImage = itemView.findViewById(R.id.layoutImage);
            layoutText = itemView.findViewById(R.id.layoutText);
            imageMessage = itemView.findViewById(R.id.imageChat);
            layoutVoice = itemView.findViewById(R.id.layoutVoice);
            layoutDoc = itemView.findViewById(R.id.layoutDoc);
            btnPlay = itemView.findViewById(R.id.btnPlayChat);
        }

        public void bind(GroupChats groupChats) {
            //get data

            String message = groupChats.getMessage();
            String senderUid = groupChats.getSender();
            String senderName = groupChats.getSenderName();
            String type = groupChats.getType();
            String url = groupChats.getUrl();
            String timestmp = groupChats.getTimestamp();
            Log.d("sender", "message : " + message);
            Log.d("sender", "senderUid : " + senderUid);
            Log.d("sender", "type : " + type);

            switch (groupChats.getType()) {
                case "TEXT":
                    layoutText.setVisibility(View.VISIBLE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.GONE);
                    layoutDoc.setVisibility(View.GONE);

                    textMessage.setText(groupChats.getMessage());
                    txtSenderName.setText(senderName);
                    break;

                case "IMAGE":
                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.VISIBLE);
                    layoutVoice.setVisibility(View.GONE);
                    layoutDoc.setVisibility(View.GONE);

                    Glide.with(context).load(groupChats.getUrl()).into(imageMessage);
                    txtImageSenderName.setText(senderName);
                    break;

                case "VOICE":
                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.GONE);
                    layoutDoc.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.VISIBLE);

                    txtAudioSenderName.setText(senderName);
                    btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24);
                    btnPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (tmpMediaPlayer != null && tmpMediaPlayer.isPlaying()) {
                                btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24);
                                tmpMediaPlayer.stop();
                            } else {
                                btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24);
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                try {
                                    mediaPlayer.setDataSource(groupChats.getUrl());
                                    mediaPlayer.prepare();
                                    mediaPlayer.start();
                                    tmpMediaPlayer = mediaPlayer;
                                    Log.d("TAG", "onClick: Audio  Group play");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24);
                                    }
                                });
                            }
                        }
                    });

                    break;

                case "DOCUMENT":

                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.GONE);
                    layoutDoc.setVisibility(View.VISIBLE);

                    txtDocSenderName.setText(senderName);
                    if (groupChats.getMessage().length() >= 25){
                        String name = groupChats.getMessage().substring(0, 25);
                        txtFileName.setText(name);
                    } else {
                        txtFileName.setText(groupChats.getMessage());
                    }

                    String chatID = groupChats.getMessageID();

                    layoutDoc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            FirebaseStorage.getInstance().getReference().child("GroupChats/Documents/" + chatID)
                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setDataAndType(uri, "*/*");
                                    context.startActivity(intent);
                                }
                            });

                        }
                    });


                    break;

            }

        }
    }

    private void getSenderName(String senderUid) {
        if (user != null) {
            firestore.collection("Users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    sName = Objects.requireNonNull(documentSnapshot.get("userName")).toString();
                    Log.d("sender", "sName : " + sName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        Log.d("getItemViewType", "senderId : " + groupChats.get(position).getSender());
        if (groupChats.get(position).getSender().equals(user.getUid())) {
            return MSG_TYPE_right;
        } else {
            return MSG_TYPE_left;
        }
    }

}
