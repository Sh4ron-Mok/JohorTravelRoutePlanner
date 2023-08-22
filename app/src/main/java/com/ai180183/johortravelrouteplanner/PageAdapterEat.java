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

import com.ai180183.johortravelrouteplanner.classes.AttractivePlaces;
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

public class PageAdapterEat extends RecyclerView.Adapter<PageAdapterEat.ViewHolder> {
    private static final String TAG = "PageAdapterEat";
    FirebaseFirestore fStore;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseUser fUser = fAuth.getCurrentUser();
    String userEmail = fUser.getEmail();

    private ArrayList<AttractivePlaces> placeList;
    private Context context;

    // creating constructor for the adapter class
    public PageAdapterEat(ArrayList<AttractivePlaces> placeList, Context context, FirebaseFirestore fStore) {
        this.placeList = placeList;
        this.context = context;
        this.fStore = fStore;
    }

    // method for filtering the recyclerview items.
    public void filterList(ArrayList<AttractivePlaces> filterllist) {
        placeList = filterllist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PageAdapterEat.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_category,parent,false);

        return new PageAdapterEat.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PageAdapterEat.ViewHolder holder, final int position) {
        final AttractivePlaces place = (AttractivePlaces) placeList.get(position);

        holder.placeName.setText(place.getPlaceName());
        holder.placeDesc.setText(place.getPlaceDesc());
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        holder.placeDate.setText(formatter.format(place.getPlaceDate()));
        Picasso.get().load(place.getPlaceImg()).into(holder.placeImg);
        if (place.getOpeningHour()==0){
            holder.operatingHour.setText("Operating Hour: 0000-"+place.getClosingHour());
        } else {
            holder.operatingHour.setText("Operating Hour: "+place.getOpeningHour()+"-"+place.getClosingHour());
        }

        if (Objects.equals(userEmail, "huifenmok@gmail.com")) {
            holder.editPlace.setVisibility(View.VISIBLE);
            holder.deletePlace.setVisibility(View.VISIBLE);
        } else {
            holder.editPlace.setVisibility(View.GONE);
            holder.deletePlace.setVisibility(View.GONE);
        }

        holder.editPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePlace(place);
            }
        });

        holder.deletePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());
                alertDialog.setTitle("Are you sure to delete?");
                alertDialog.setMessage("Place: "+place.getPlaceName());
                alertDialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteLog(place.getPlaceID(), position);
                    }
                });
                AlertDialog dialog = alertDialog.create();
                dialog.show();
            }
        });
    }

    private void deleteLog(String placeID, final int position) {
        fStore.collection("attractivePlaces").document(placeID)
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                placeList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, placeList.size());
                Toast.makeText(context,"The place is deleted.",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"onFailed: Cannot delete the place, "+e.toString());
            }
        });
    }

    private void updatePlace(AttractivePlaces log) {
        Intent intent = new Intent(context, AddNewPlace.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("UpdatePlaceId", log.getPlaceID());
        intent.putExtra("UpdatePlaceName", log.getPlaceName());
        intent.putExtra("UpdatePlaceDesc", log.getPlaceDesc());
        intent.putExtra("UpdatePlaceImg", log.getPlaceImg());
        intent.putExtra("UpdatePlaceDate", log.getPlaceDate());
        intent.putExtra("UpdateCategory", log.getCategory());
        intent.putExtra("UpdatePlaceLatitude", log.getLat());
        intent.putExtra("UpdatePlaceLongitude", log.getLng());
        intent.putExtra("UpdateOpeningHour", log.getOpeningHour());
        intent.putExtra("UpdateClosingHour", log.getClosingHour());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView placeName, placeDesc, placeDate, operatingHour;
        private final ImageView placeImg;
        private final Button editPlace, deletePlace;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.placeName);
            placeDesc = itemView.findViewById(R.id.placeDesc);
            placeImg = itemView.findViewById(R.id.placeImg);
            placeDate = itemView.findViewById(R.id.placeDate);
            editPlace = itemView.findViewById(R.id.editPlace);
            deletePlace = itemView.findViewById(R.id.deletePlace);
            operatingHour = itemView.findViewById(R.id.operateTime);
        }
    }

}