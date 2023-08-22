package com.ai180183.johortravelrouteplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ai180183.johortravelrouteplanner.classes.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PageAdapterUsers extends RecyclerView.Adapter<PageAdapterUsers.ViewHolder> {
    private static final String TAG = "PageAdapterUsers";
    FirebaseFirestore fStore;

    private ArrayList<Users> userList;
    private Context context;

    // creating constructor for the adapter class
    public PageAdapterUsers(ArrayList<Users> userList, Context context, FirebaseFirestore fStore) {
        this.userList = userList;
        this.context = context;
        this.fStore = fStore;
    }

    // method for filtering the recyclerview items.
    public void filterList(ArrayList<Users> filterllist) {
        userList = filterllist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PageAdapterUsers.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_addnewadmin,parent,false);

        return new PageAdapterUsers.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageAdapterUsers.ViewHolder holder, final int position) {
        final Users userAdd = (Users) userList.get(position);

        Log.d(TAG,"username: "+userAdd.getFullName());
        holder.listViewText.setText(userAdd.getFullName());
        if (userAdd.getImgUrl() != null) {
            Picasso.get().load(userAdd.getImgUrl()).into(holder.listViewImg);
        }

        holder.upgradeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userAdd.setUser(false);
                Log.d(TAG, "Here: "+userAdd.getFullName()+" and "+userAdd.getUserId());
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());
                alertDialog.setTitle("Are you sure to change this user to admin?");
                alertDialog.setMessage("Name: "+userAdd.getFullName());
                alertDialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        upgradeUser(userAdd.getUserId(),userAdd.getUser());
                    }
                });
                AlertDialog dialog = alertDialog.create();
                dialog.show();
            }
        });
    }

    private void upgradeUser(String userID, final Boolean user) {
        fStore.collection("users").document(userID)
                .update("isUser", false)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context,"User type of "+userID + " is changed.",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"onFailed: Cannot change user type, "+e.toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView listViewText;
        private final ImageView listViewImg;
        private final Button upgradeUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listViewImg = itemView.findViewById(R.id.listViewImg);
            listViewText = itemView.findViewById(R.id.listViewText);
            upgradeUser = itemView.findViewById(R.id.upgradeUser);

        }
    }

}