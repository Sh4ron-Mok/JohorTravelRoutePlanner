package com.ai180183.johortravelrouteplanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Queue;

public class DirectionsRouteAdapter extends RecyclerView.Adapter<DirectionsRouteAdapter.DirectionsRouteViewHolder> {

    private MapsActivity mActivity;
    public ArrayList<String> mRoute;
    public ArrayList<JSONParser.PlaceDetails> mPlaceDetails;
    public ArrayList<Integer> mMarkerColors;
    public Queue<Integer> mUnusedColors;
    public ArrayList<Integer> mOrder;
    DirectionsRouteAdapter(MapsActivity activity, ArrayList<String> route,
                           ArrayList<JSONParser.PlaceDetails> placeDetails, ArrayList<Integer> markerColors,
                           Queue<Integer> unusedColors, ArrayList<Integer> order) {
        mActivity = activity;
        mRoute = route;
        mPlaceDetails = placeDetails;
        mMarkerColors = markerColors;
        mUnusedColors = unusedColors;
        mOrder = order;
    }

    @NonNull
    @Override
    public DirectionsRouteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;

        if (viewType == 1) {
            view = inflater.inflate(R.layout.start_end_view, viewGroup, false);
            ImageView startIcon = view.findViewById(R.id.start_end_icon);
            startIcon.setImageDrawable(mActivity.getDrawable(R.drawable.ic_baseline_home_24));
            TextView startText = view.findViewById(R.id.start_end_text);
            startText.setText(mActivity.getString(R.string.start));
        } else if (viewType == 2) {
            view = inflater.inflate(R.layout.start_end_view, viewGroup, false);
            ImageView endIcon = view.findViewById(R.id.start_end_icon);
            endIcon.setImageDrawable(mActivity.getDrawable(R.drawable.ic_baseline_weekend_24));
            TextView endText = view.findViewById(R.id.start_end_text);
            endText.setText(mActivity.getString(R.string.end));
        } else if (viewType == 3) {
            view = inflater.inflate(R.layout.directions_no_locations_added_view, viewGroup, false);
        } else {
            view = inflater.inflate(R.layout.directions_location_view, viewGroup, false);
        }

        return new DirectionsRouteViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull DirectionsRouteViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            position--;
            Bitmap bitmap = mActivity.createMarker(mMarkerColors.get(mOrder.get(position)),
                    position);
            holder.mMarker.setImageBitmap(bitmap);
            holder.mPlaceTitle.setText(Html.fromHtml(mActivity.getString(
                    R.string.place_title, mRoute.get(mOrder.get(position)))));
            if (mPlaceDetails.get(position).getAddress() != null) {
                holder.mPlaceAddress.setText(mPlaceDetails.get(position).getAddress());
                holder.mPlaceAddress.setVisibility(View.VISIBLE);
            } else {
                holder.mPlaceAddress.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mPlaceDetails.size() == 0 ? 3 :
                mPlaceDetails.size()+2);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 1;
        }
        if (position == getItemCount()-1) {
            return 2;
        }
        if (position == 1 && mRoute.size() == 0) {
            return 3;
        }
        return 0;
    }

    public class DirectionsRouteViewHolder extends RecyclerView.ViewHolder {

        ImageView mMarker;
        TextView mPlaceTitle;
        TextView mPlaceAddress;

        public DirectionsRouteViewHolder(@NonNull View itemView) {
            super(itemView);

            mMarker = itemView.findViewById(R.id.marker);
            mPlaceTitle = itemView.findViewById(R.id.placeTitle);
            mPlaceAddress = itemView.findViewById(R.id.placeAddress);
        }
    }

}

