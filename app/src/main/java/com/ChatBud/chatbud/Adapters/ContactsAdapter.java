package com.ChatBud.chatbud.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ChatBud.chatbud.ChatsActivity;
import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.R;
import com.ChatBud.chatbud.interfaces.OnContactClickListener;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private List<Users> list;
    private Context context;
    private OnContactClickListener onContactClickListener;

    public ContactsAdapter(List<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_contact_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = list.get(position);

        holder.username.setText(users.getUserName());

        if (users.getBio().isEmpty()) {
            holder.lstmsg.setText("Hey there, This is MediaClub");
        } else{
            holder.lstmsg.setText(users.getBio());
        }

        if (users.getImageProfile().isEmpty()) {
            holder.imageProfile.setImageResource(R.drawable.icon_male_ph);
        } else{
            Picasso.get().load(users.getImageProfile()).into(holder.imageProfile);
        }

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onContactClickListener.setOnUserClickListener(users);
//            }
//        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ChatsActivity.class);

                intent.putExtra("userID" , users.getUserID());
                intent.putExtra("userProfile" , users.getImageProfile());
                intent.putExtra("Bio" , users.getBio());
                intent.putExtra("userName" , users.getUserName());
                intent.putExtra("phoneNo", users.getUserPhone());
                context.startActivity(intent);

            }
        });

    }

    public void setOnUserClick(OnContactClickListener onContactClickListener)
    {
        this.onContactClickListener = onContactClickListener;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imageProfile;
        private TextView username, lstmsg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.profile_image);
            lstmsg = itemView.findViewById(R.id.lastMessage);
            username = itemView.findViewById(R.id.userNameList);

        }
    }
}
