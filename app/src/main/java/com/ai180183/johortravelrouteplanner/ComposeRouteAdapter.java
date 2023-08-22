package com.ai180183.johortravelrouteplanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Queue;

public class ComposeRouteAdapter extends RecyclerView.Adapter<ComposeRouteAdapter.ComposeRouteViewHolder>
        implements DragDropSwipeHelper.ActionCompletionContract {

    private MapsActivity mActivity;
    ArrayList<String> mRoute;
    private ArrayList<JSONParser.PlaceDetails> mPlaceDetails;
    private ArrayList<Boolean> mFlagged;
    private ArrayList<Integer> mMarkerColors;
    private Queue<Integer> mUnusedColors;
    ComposeRouteAdapter(MapsActivity activity, ArrayList<String> route,
                        ArrayList<JSONParser.PlaceDetails> placeDetails, ArrayList<Boolean> flagged,
                        ArrayList<Integer> markerColors, Queue<Integer> unusedColors) {
        mActivity = activity;
        mRoute = route;
        mPlaceDetails = placeDetails;
        mFlagged = flagged;
        mMarkerColors = markerColors;
        mUnusedColors = unusedColors;
    }

    @NonNull
    @Override
    public ComposeRouteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
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
            view = inflater.inflate(R.layout.compose_no_locations_added_view, viewGroup, false);
        } else {
            view = inflater.inflate(R.layout.compose_location_view, viewGroup, false);
        }

        return new ComposeRouteViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull final ComposeRouteViewHolder viewHolder, int position) {
        if (getItemViewType(position) == 0) {
            position--;
            Bitmap bitmap = mActivity.createMarker(mMarkerColors.get(position), position);
            viewHolder.mMarker.setImageBitmap(bitmap);
            viewHolder.mPlaceTitle.setText(Html.fromHtml(mActivity.getString(
                    R.string.place_title, mRoute.get(position))));
            if (mPlaceDetails.get(position).getAddress() != null) {
                viewHolder.mPlaceAddress.setText(mPlaceDetails.get(position).getAddress());
                viewHolder.mPlaceAddress.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mPlaceAddress.setVisibility(View.GONE);
            }
            setFlag(viewHolder, position, false);
            viewHolder.mLockedInPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFlag(viewHolder, viewHolder.getAdapterPosition()-1, true);
                }
            });
            viewHolder.mMoreOptions.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(mActivity, v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.compose_route_individual_popup_menu, popup.getMenu());
                    popup.show();

                    @SuppressLint("RestrictedApi") MenuPopupHelper menuHelper = new MenuPopupHelper(mActivity,
                            (MenuBuilder) popup.getMenu(), v);
                    menuHelper.setForceShowIcon(true);
                    menuHelper.show();

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.insert_location_above:
                                    mActivity.mAddLocationContainer.setVisibility(View.VISIBLE);
                                    mActivity.mapFragment.getView().setAlpha(0.4f);
                                    mActivity.mSearchView.requestFocus();
                                    mActivity.mAddPosition = viewHolder.getAdapterPosition()-1;
                                    mActivity.mAddLocationSubmitBtn.setText(mActivity.getString(
                                            R.string.add_location));
                                    return true;

                                case R.id.edit_location:
                                    int position = viewHolder.getAdapterPosition()-1;
                                    mActivity.mAddLocationContainer.setVisibility(View.VISIBLE);
                                    mActivity.mapFragment.getView().setAlpha(0.4f);
                                    mActivity.mSearchView.setQuery(mActivity.mComposeRoute.get(
                                            position),false);
                                    mActivity.mSearchView.requestFocus();
                                    mActivity.mPlaceIdsAdd = mActivity.mComposePlaceDetails.get(
                                            position).getPlaceId();
                                    mActivity.mAddPosition = -1;
                                    mActivity.mEditPosition = position;
                                    mActivity.mAddLocationSubmitBtn.setText(mActivity.getString(
                                            R.string.edit_location));
                                    return true;

                                case R.id.remove_location:
                                    remove(viewHolder.getAdapterPosition()-1);
                                    return true;

                                default:
                                    return false;
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (mRoute.size() == 0 ? 3 : mRoute.size()+2);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        if (getItemViewType(oldPosition) == 0 && getItemViewType(newPosition) == 0) {
            oldPosition--;
            newPosition--;

            String location = mRoute.get(oldPosition);
            mRoute.remove(oldPosition);
            mRoute.add(newPosition, location);

            JSONParser.PlaceDetails placeDetails = mPlaceDetails.get(oldPosition);
            mPlaceDetails.remove(oldPosition);
            mPlaceDetails.add(newPosition, placeDetails);

            boolean flagged = mFlagged.get(oldPosition);
            mFlagged.remove(oldPosition);
            mFlagged.add(newPosition, flagged);

            int markerColor = mMarkerColors.get(oldPosition);
            mMarkerColors.remove(oldPosition);
            mMarkerColors.add(newPosition, markerColor);

            Bitmap oldBitmap = mActivity.createMarker(mMarkerColors.get(oldPosition), oldPosition);
            Bitmap newBitmap = mActivity.createMarker(mMarkerColors.get(newPosition), newPosition);
            ((ComposeRouteViewHolder) mActivity.mBottomSheetFragmentEditOptimize.mComposeRouteLocations.
                    findViewHolderForAdapterPosition(oldPosition+1)).mMarker.setImageBitmap(newBitmap);
            ((ComposeRouteViewHolder) mActivity.mBottomSheetFragmentEditOptimize.mComposeRouteLocations.
                    findViewHolderForAdapterPosition(newPosition+1)).mMarker.setImageBitmap(oldBitmap);

            Marker temp = mActivity.mComposeMarkers.get(oldPosition);
            mActivity.mComposeMarkers.remove(oldPosition);
            mActivity.mComposeMarkers.add(newPosition, temp);
            if (mActivity.mComposeMarkers.get(oldPosition) != null) {
                mActivity.mComposeMarkers.get(oldPosition).setIcon(
                        BitmapDescriptorFactory.fromBitmap(oldBitmap));
            }
            if (mActivity.mComposeMarkers.get(newPosition) != null) {
                mActivity.mComposeMarkers.get(newPosition).setIcon(
                        BitmapDescriptorFactory.fromBitmap(newBitmap));
            }

            oldPosition++;
            newPosition++;
            notifyItemMoved(oldPosition, newPosition);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewSwiped(int position) {
        if (getItemViewType(position) == 0) {
            position--;

            remove(position);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void remove(int position) {
        mRoute.remove(position);
        mPlaceDetails.remove(position);
        mFlagged.remove(position);
        mUnusedColors.add(mMarkerColors.get(position));
        mMarkerColors.remove(position);

        if (mActivity.mComposeMarkers.get(position) != null) {
            mActivity.mComposeMarkers.get(position).remove();
        }
        mActivity.mComposeMarkers.remove(position);

        for (int i=position; i<mRoute.size(); i++) {
            if (mActivity.mComposeMarkers.get(i) != null) {
                Bitmap bitmap = mActivity.createMarker(mMarkerColors.get(i), i);
                mActivity.mComposeMarkers.get(i).setIcon(
                        BitmapDescriptorFactory.fromBitmap(bitmap));
            }
        }

        TextView numLocationsAdded = mActivity.findViewById(R.id.num_locations_added);
        numLocationsAdded.setText(mActivity.getString((mActivity.mComposeRoute.size() == 1
                        ? R.string.num_location_added : R.string.num_locations_added),
                Integer.toString(mActivity.mComposeRoute.size())));

        position++;
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mRoute.size());
    }

    private void setFlag(ComposeRouteViewHolder viewHolder, int position, boolean clicked) {
        if (clicked) mFlagged.set(position, !mFlagged.get(position));
        if (mFlagged.get(position)) {
            viewHolder.mLockedInPlace.setImageResource(R.drawable.ic_baseline_lock_24);
        } else {
            viewHolder.mLockedInPlace.setImageResource(R.drawable.ic_baseline_lock_open_24);
        }
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

    public class ComposeRouteViewHolder extends RecyclerView.ViewHolder {

        ImageView mMarker;
        TextView mPlaceTitle;
        TextView mPlaceAddress;
        ImageButton mLockedInPlace;
        ImageButton mMoreOptions;

        public ComposeRouteViewHolder(@NonNull View itemView) {
            super(itemView);

            mMarker = itemView.findViewById(R.id.marker);
            mPlaceTitle = itemView.findViewById(R.id.placeTitle);
            mPlaceAddress = itemView.findViewById(R.id.placeAddress);
            mLockedInPlace = itemView.findViewById(R.id.lockedInPlace);
            mMoreOptions = itemView.findViewById(R.id.moreOptions);
        }
    }
}
