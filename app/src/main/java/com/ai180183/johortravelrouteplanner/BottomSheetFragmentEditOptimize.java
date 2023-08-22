package com.ai180183.johortravelrouteplanner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class BottomSheetFragmentEditOptimize extends Fragment {

    private MapsActivity mActivity;
    public ComposeRouteAdapter mComposeRouteAdapter;
    public RecyclerView mComposeRouteLocations;
    private TextView mNumLocationsAdded;
    private static final int DELAY_TIME = 300;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = (MapsActivity) getActivity();
        return inflater.inflate(R.layout.bottom_sheet_fragment_edit_optimize, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mComposeRouteLocations = mActivity.findViewById(R.id.composeRouteLocations);
        ImageButton moreOptions = getView().findViewById(R.id.moreOptions);
        mNumLocationsAdded = mActivity.findViewById(R.id.num_locations_added);
        Button optimizeButton = getView().findViewById(R.id.optimize);

        mActivity.mSlidingUpPanelLayout.setDragView(R.id.header);

        mComposeRouteAdapter = new ComposeRouteAdapter(mActivity, mActivity.mComposeRoute,
                mActivity.mComposePlaceDetails, mActivity.mComposeFlagged,
                mActivity.mComposeMarkerColors, mActivity.mComposeUnusedColors);
        DragDropSwipeHelper dragDropSwipeHelper = new DragDropSwipeHelper(mComposeRouteAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragDropSwipeHelper);
        mComposeRouteLocations.setLayoutManager(new LinearLayoutManager(mActivity));
        mComposeRouteLocations.setAdapter(mComposeRouteAdapter);
        itemTouchHelper.attachToRecyclerView(mComposeRouteLocations);

        mNumLocationsAdded.setText(mActivity.getString((mActivity.mComposeRoute.size() == 1
                        ? R.string.num_location_added : R.string.num_locations_added),
                Integer.toString(mActivity.mComposeRoute.size())));

        moreOptions.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mActivity, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.compose_route_overall_popup_menu, popup.getMenu());
                popup.show();

                @SuppressLint("RestrictedApi") MenuPopupHelper menuHelper = new MenuPopupHelper(mActivity,
                        (MenuBuilder) popup.getMenu(), v);
                menuHelper.setForceShowIcon(true);
                menuHelper.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.remove_all_locations:
                                int size = mActivity.mComposeRoute.size();
                                mActivity.mComposeRoute.clear();
                                mActivity.mComposePlaceDetails.clear();
                                mActivity.mComposeFlagged.clear();
                                mActivity.mComposeUnusedColors.addAll(
                                        mActivity.mComposeMarkerColors);
                                mActivity.mComposeMarkerColors.clear();
                                for (int i=0; i<mActivity.mComposeMarkers.size(); i++) {
                                    if (mActivity.mComposeMarkers.get(i) != null) {
                                        mActivity.mComposeMarkers.get(i).remove();
                                    }
                                }
                                mActivity.mComposeMarkers.clear();
                                mComposeRouteAdapter.notifyItemRangeRemoved(0, size);
                                mNumLocationsAdded.setText(mActivity.getString(
                                        R.string.num_locations_added,
                                        Integer.toString(mActivity.mComposeRoute.size())));
                                return true;

                            default:
                                return false;
                        }
                    }
                });
            }
        });

        optimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if  (mActivity.mComposeRoute != null && mActivity.mComposeRoute.size() >= 2) {
                    mActivity.mDirectionsRoute.clear();
                    mActivity.mDirectionsRoute.addAll(mActivity.mComposeRoute);
                    mActivity.mDirectionsPlaceDetails.clear();
                    mActivity.mDirectionsPlaceDetails.addAll(mActivity.mComposePlaceDetails);
                    mActivity.mDirectionsFlagged.clear();
                    mActivity.mDirectionsFlagged.addAll(mActivity.mComposeFlagged);
                    mActivity.mDirectionsMarkerColors.clear();
                    mActivity.mDirectionsMarkerColors.addAll(mActivity.mComposeMarkerColors);
                    mActivity.mDirectionsUnusedColors.clear();
                    mActivity.mDirectionsUnusedColors.addAll(mActivity.mComposeUnusedColors);

                    mActivity.mOptimizeClicked = true;
                    mActivity.mWhoseMarkers = R.id.directions;

                    mActivity.mBottomNavigationView.setSelectedItemId(R.id.directions);
                } else {
                    Toast.makeText(mActivity, mActivity.getString(R.string.minimum_2_locations),
                            Toast.LENGTH_SHORT).show();
                    Log.d("MapsActivity",mActivity.getString(R.string.minimum_2_locations));
                    mActivity.mAddLocationContainer.setVisibility(View.VISIBLE);
                    mActivity.mapFragment.getView().setAlpha(0.4f);
                    mActivity.mSearchView.requestFocus();
                    mActivity.mAddPosition = mActivity.mComposeRoute.size();
                    if (mActivity.mAddLocationSubmitBtn != null) {
                        mActivity.mAddLocationSubmitBtn.setText(getString(R.string.add_location));
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        }
                    }, DELAY_TIME);
                }
            }
        });
    }
}
