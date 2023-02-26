package com.ChatBud.chatbud.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ChatBud.chatbud.Models.Users;
import com.ChatBud.chatbud.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddParticipantAdapter extends RecyclerView.Adapter<AddParticipantAdapter.HolderAddParticipants> {

    private static final String TAG = "";
    Context context;
    ArrayList<Users> usersList;
    String groupId, myGroupRole;

    public AddParticipantAdapter(Context context, ArrayList<Users> usersList, String groupId, String myGroupRole) {
        this.context = context;
        this.usersList = usersList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }


    @NonNull
    @Override
    public HolderAddParticipants onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_add_participant, parent, false);
        return new HolderAddParticipants(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAddParticipants holder, int position) {
        //get data
        Users modelUser = usersList.get(position);
        String uid = modelUser.getUserID();
        String name = modelUser.getUserName();
        String image = modelUser.getImageProfile();
        String bio = modelUser.getBio();

        Log.d("TAG", "onSuccess: UserList in adapter: "+usersList);

        //set data
        holder.nameTv.setText(name);
        //bio
        if (bio.equals("null")) {
            holder.bioTv.setText("Hey, This is ChatBud.");
        } else {
            holder.bioTv.setText(bio);
        }
        //imageProfile
        if (image.isEmpty()) {
            holder.icon.setImageResource(R.drawable.icon_male_ph);
        } else {
            Picasso.get().load(image).placeholder(R.drawable.icon_male_ph).into(holder.icon);
        }

        //checkIfAlreadyExist(modelUser, holder);

        Log.d(TAG, "onBindViewHolder: groupId: "+groupId);
        Log.d(TAG, "onBindViewHolder: uid: "+uid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //already exist
                        String hisRole = ""+snapshot.child("role").getValue();
                        Log.d("TAG", "onDataChange: HisRole in adapter: "+hisRole);
                        if (hisRole.equals("null")){
                            holder.roleTv.setText("(Not a member of this group)");
                        } else {
                            holder.roleTv.setText("("+hisRole+")");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.roleTv.setText("");
                    }
                });

        Log.d("TAG", "onDataChange: MyGroupRole in adapter: "+myGroupRole);

        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("TAG", "onDataChange: uid: "+uid);
                Log.d("TAG", "onDataChange: MyGroupRole in adapter: "+myGroupRole);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    //already exist
                                    String hisPreviousRole = "" + snapshot.child("role").getValue();

                                    //options to display in dialog
                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Options");
                                    if (myGroupRole.equals("creator")) {
                                        if (hisPreviousRole.equals("admin")) {
                                            //im creator , he is admin
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle clicks
                                                    if (which == 0) {
                                                        //remove admin
                                                        removeAdmin(modelUser);
                                                    } else {
                                                        //remove user
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")) {
                                            //im creator , he is participant
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle clicks
                                                    if (which == 0) {
                                                        //make admin
                                                        MakeAdmin(modelUser);
                                                    } else {
                                                        //remove user
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                    else if (myGroupRole.equals("admin")) {
                                        if (hisPreviousRole.equals("creator")) {
                                            //im admin, he is creator
                                            Toast.makeText(context, "Creator of group...", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (hisPreviousRole.equals("admin")) {
                                            //he is admin too
                                            options = new String[]{"Remove admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle clicks
                                                    if (which == 0) {
                                                        //remove admin
                                                        removeAdmin(modelUser);
                                                    } else {
                                                        //remove user
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if (hisPreviousRole.equals("participant")) {
                                            //im admin , he is participant
                                            options = new String[]{"make admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle clicks
                                                    if (which == 0) {
                                                        //make admin
                                                        MakeAdmin(modelUser);
                                                    } else {
                                                        //remove user
                                                        removeParticipant(modelUser);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                }
                                else {
                                    //user is not participant of this group add user
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group")
                                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //add user in group
                                                    addParticipant(modelUser);
                                                }
                                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                                }
                            }

                @Override
                public void onCancelled (@NonNull DatabaseError error){

                }
            });
        }
    });

}

    private void addParticipant(Users modelUser) {
        //setup user data
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String , String> hashMap = new HashMap<>();
        hashMap.put("uid", modelUser.getUserID());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getUserID()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //user added
                        Toast.makeText(context, "User added successfully.", Toast.LENGTH_SHORT).show();
                        //Intent intent = new Intent(context, GroupsFragment.class);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Failed to add user
                Toast.makeText(context, "failed to add user."+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void MakeAdmin(Users modelUser) {
        //setup data - make admin - change role to admin
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUser.getUserID()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //user is now admin
                        Toast.makeText(context, "User is now admin.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update
                        Toast.makeText(context, "Failed to make admin"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeParticipant(Users modelUser) {
        // remove participant
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUser.getUserID()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //User Removed
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed to remove user
                        Toast.makeText(context, "Failed to remove user."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeAdmin(Users modelUser) {
        //setup data - remove admin - change role to participant
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUser.getUserID()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //user is now admin
                        Toast.makeText(context, "User is now only member of the group.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update
                        Toast.makeText(context, "Failed to remove admin"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfAlreadyExist(Users modelUser, HolderAddParticipants holder) {



    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

class HolderAddParticipants extends RecyclerView.ViewHolder {

    private CircleImageView icon;
    private TextView nameTv, bioTv, roleTv;

    public HolderAddParticipants(@NonNull View itemView) {
        super(itemView);

        icon = itemView.findViewById(R.id.icon);
        nameTv = itemView.findViewById(R.id.nameTv);
        bioTv = itemView.findViewById(R.id.bioTv);
        roleTv = itemView.findViewById(R.id.roleTv);
    }
}

}
