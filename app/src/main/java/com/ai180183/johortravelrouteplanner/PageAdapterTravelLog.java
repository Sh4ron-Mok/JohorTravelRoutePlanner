package com.ai180183.johortravelrouteplanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.ai180183.johortravelrouteplanner.classes.TravelLog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class PageAdapterTravelLog extends RecyclerView.Adapter<PageAdapterTravelLog.ViewHolder> {
    private static final String TAG = "PageAdapterTravelLog";
    FirebaseFirestore fStore;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseUser fUser = fAuth.getCurrentUser();
    String userEmail = fUser.getEmail();

    private ArrayList<TravelLog> logList;
    private Context context;

    // creating constructor for the adapter class
    public PageAdapterTravelLog(ArrayList<TravelLog> logList, Context context, FirebaseFirestore fStore) {
        this.logList = logList;
        this.context = context;
        this.fStore = fStore;
    }

    // method for filtering the recyclerview items.
    public void filterList(ArrayList<TravelLog> filterllist) {
        logList = filterllist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PageAdapterTravelLog.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_travellog,parent,false);

        return new PageAdapterTravelLog.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PageAdapterTravelLog.ViewHolder holder, final int position) {
        final TravelLog log = (TravelLog) logList.get(position);

        Log.d(TAG,"isUser: "+log.getUser());
        holder.logEmail.setText("By: "+log.getUserEmail());
        holder.logTitle.setText(log.getLogTitle());
        holder.logContent.setText(log.getLogContent());
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        holder.logDate.setText(formatter.format(log.getLogDate()));
        if (log.getImgUrl() != null) {
            holder.logImg.setVisibility(View.VISIBLE);
            Picasso.get().load(log.getImgUrl()).into(holder.logImg);
        }

        if (!userEmail.equals(log.getUserEmail())) {
            holder.editTravelLog.setVisibility(View.GONE);
            holder.deleteTravelLog.setVisibility(View.GONE);
        }

        if (Objects.equals(userEmail, "huifenmok@gmail.com")) {
            holder.editTravelLog.setVisibility(View.VISIBLE);
            holder.deleteTravelLog.setVisibility(View.VISIBLE);
        }

        holder.editTravelLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTravelLog(log);
            }
        });

        holder.deleteTravelLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());
                alertDialog.setTitle("Are you sure to delete?");
                alertDialog.setMessage("Title: "+log.getLogTitle());
                alertDialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteLog(log.getLogID(), position);
                    }
                });
                AlertDialog dialog = alertDialog.create();
                dialog.show();
            }
        });
    }

    private void deleteLog(String logID, final int position) {
        fStore.collection("travelLog").document(logID)
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                logList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, logList.size());
                Toast.makeText(context,"Travel log is deleted.",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"onFailed: Cannot delete travel log, "+e.toString());
            }
        });
    }

    private void updateTravelLog(TravelLog log) {
        Intent intent = new Intent(context, AddTravelLog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("UpdateLogId", log.getLogID());
        intent.putExtra("UpdateLogTitle", log.getLogTitle());
        intent.putExtra("UpdateLogContent", log.getLogContent());
        intent.putExtra("UpdateLogImg", log.getImgUrl());
        intent.putExtra("UpdateLogDate", log.getLogDate());
        intent.putExtra("UpdateUserEmail", log.getUserEmail());
        intent.putExtra("UpdateUserType", log.getUser());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView logTitle, logContent, logDate, logEmail;
        private final ImageView logImg;
        private final Button editTravelLog, deleteTravelLog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            logTitle = itemView.findViewById(R.id.logText_title);
            logContent = itemView.findViewById(R.id.logText_description);
            logImg = itemView.findViewById(R.id.logImg);
            logDate = itemView.findViewById(R.id.logText_date);
            logEmail = itemView.findViewById(R.id.logText_email);
            editTravelLog = itemView.findViewById(R.id.editTravelLog);
            deleteTravelLog = itemView.findViewById(R.id.deleteTravelLog);
        }
    }

}