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

import com.ChatBud.chatbud.Models.Chats;
import com.ChatBud.chatbud.R;
import com.ChatBud.chatbud.ReviewImageActivity;
import com.ChatBud.chatbud.StartActivity;
import com.ChatBud.chatbud.service.AudioService;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {
    private List<Chats> list;
    private Context context;
    private String recID;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private FirebaseUser firebaseUser;
    private ImageButton tmpBtnPlay;
    private AudioService audioService;

    private MediaPlayer tmpMediaPlayer;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    public ChatsAdapter(List<Chats> list, Context context, String recID) {
        this.list = list;
        this.recID = recID;
        this.context = context;
        this.audioService = new AudioService(context);
    }

    public void setList(List<Chats> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));

        Chats chats = list.get(position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                /*new AlertDialog.Builder(context).setTitle("Delete").setMessage("Are you sure that you want to delete message...").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sender = FirebaseAuth.getInstance().getUid() + recID;

                        Log.d("TAG", "onClick: chatID:"+chats.getChatId());
                        Log.d("TAG", "onClick: sender:"+sender);

                        try{
                            ref.child("Chats").child(sender).child(chats.getChatId()).setValue(null);
                            Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Log.d("TAG", "onClick: Chat delete failed."+e.getMessage());
                        }

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();*/

                String senderID = chats.getSenderID();
                String[] options;

                if (senderID.equals(firebaseUser.getUid())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Message");
                    options = new String[]{"Delete for Everyone", "Delete for me"};
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0){
                                //Delete fot everyone
                                String senderRoom = FirebaseAuth.getInstance().getUid() + recID;
                                String receiverRoom = recID + FirebaseAuth.getInstance().getUid();

                                try{
                                    ref.child("Chats").child(senderRoom).child(chats.getChatId()).setValue(null);
                                    ref.child("Chats").child(receiverRoom).child(chats.getChatId()).setValue(null);
                                }catch (Exception e){
                                    Log.d("TAG", "onClick: Chat delete failed."+e.getMessage());
                                }

                            } else {
                                //delete for me
                                String sender = FirebaseAuth.getInstance().getUid() + recID;

                                Log.d("TAG", "onClick: chatID:"+chats.getChatId());
                                Log.d("TAG", "onClick: sender:"+sender);

                                try{
                                    ref.child("Chats").child(sender).child(chats.getChatId()).setValue(null);
                                    Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();
                                }catch (Exception e){
                                    Log.d("TAG", "onClick: Chat delete failed."+e.getMessage());
                                }
                            }
                        }
                    }).show();
                } else {
                    new AlertDialog.Builder(context).setTitle("Delete").setMessage("Are you sure that you want to delete message...")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String sender = FirebaseAuth.getInstance().getUid() + recID;

                            Log.d("TAG", "onClick: chatID:"+chats.getChatId());
                            Log.d("TAG", "onClick: sender:"+sender);

                            try{
                                ref.child("Chats").child(sender).child(chats.getChatId()).setValue(null);
                                Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                Log.d("TAG", "onClick: Chat delete failed."+e.getMessage());
                            }

                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }


                return false;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chats.getType().equals("IMAGE")){
                    String image = chats.getUrl();
                    Intent intent = new Intent(context, ReviewImageActivity.class);
                    intent.putExtra("image", image);
                    context.startActivity(intent);
                } else if (chats.getType().equals("DOCUMENT")){
                    String chatID = chats.getChatId();
                    FirebaseStorage.getInstance().getReference().child("Chats/Documents/" + chatID)
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage, txtFileName;
        private LinearLayout layoutText, layoutImage, layoutVoice, layoutDoc;
        private ImageView imageMessage;
        private ImageView btnPlay, btnPause;
        private ViewHolder tmpHolder;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textMessage = itemView.findViewById(R.id.tv_text_message);
            txtFileName = itemView.findViewById(R.id.txtFileName);
            layoutImage = itemView.findViewById(R.id.layout_image);
            layoutText = itemView.findViewById(R.id.layout_text);
            imageMessage = itemView.findViewById(R.id.image_chat);
            layoutVoice = itemView.findViewById(R.id.layout_voice);
            layoutDoc = itemView.findViewById(R.id.layout_Doc);
            btnPlay = itemView.findViewById(R.id.btn_play_chat);
        }
        void bind(final Chats chats){
            //Check chat type..

            switch (chats.getType()){
                case "TEXT" :
                    layoutText.setVisibility(View.VISIBLE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.GONE);
                    layoutDoc.setVisibility(View.GONE);

                    textMessage.setText(chats.getTextMessage());

                    break;
                case "IMAGE" :
                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.VISIBLE);
                    layoutVoice.setVisibility(View.GONE);
                    layoutDoc.setVisibility(View.GONE);

                    Glide.with(context).load(chats.getUrl()).into(imageMessage);
                    break;

                case "VOICE" :
                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.VISIBLE);
                    layoutDoc.setVisibility(View.GONE);

                    btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24);
                    btnPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (tmpMediaPlayer!=null && tmpMediaPlayer.isPlaying()){
                                btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24);
                                tmpMediaPlayer.stop();
                            } else {
                                btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24);
                                // btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                try {
                                    mediaPlayer.setDataSource(chats.getUrl());
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

                case "DOCUMENT" :

                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.GONE);
                    layoutDoc.setVisibility(View.VISIBLE);

                    if (chats.getTextMessage().length() >= 25){
                        String name = chats.getTextMessage().substring(0, 25);
                        txtFileName.setText(name);
                    } else {
                        txtFileName.setText(chats.getTextMessage());
                    }

//                    String chatID = chats.getChatId();
//
//                    layoutDoc.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//
//                            FirebaseStorage.getInstance().getReference().child("Chats/Documents/" + chatID)
//                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//                                    Intent intent = new Intent();
//                                    intent.setAction(Intent.ACTION_VIEW);
//                                    intent.setDataAndType(uri, "*/*");
//                                    context.startActivity(intent);
//                                }
//                            });
//
//                        }
//                    });

                    break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (list.get(position).getSenderID().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else
        {
            return MSG_TYPE_LEFT;
        }
    }
}