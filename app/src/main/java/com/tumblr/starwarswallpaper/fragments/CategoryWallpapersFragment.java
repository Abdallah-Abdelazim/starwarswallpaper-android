package com.tumblr.starwarswallpaper.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tumblr.starwarswallpaper.R;
import com.tumblr.starwarswallpaper.adapters.WallpapersAdapter;
import com.tumblr.starwarswallpaper.data.FirebaseDatabaseContract;
import com.tumblr.starwarswallpaper.models.Wallpaper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WallpaperCallbacks} interface
 * to handle interaction events.
 * Use the {@link CategoryWallpapersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryWallpapersFragment extends Fragment
        implements WallpapersAdapter.WallpaperItemClickListener {

    private static final String ARG_CATEGORY_KEY = "ARG_CATEGORY_KEY";
    private static final String STATE_WALLPAPERS = "STATE_WALLPAPERS";

    private String categoryKey;
    private WallpaperCallbacks wallpaperCallbacks;

    @BindView(R.id.rv_wallpapers) RecyclerView wallpapersRecyclerView;
    @BindView(R.id.pb_loading_wallpapers) ProgressBar loadingProgressBar;
    @BindInt(R.integer.grid_span_count) int gridSpanCount;

    private Unbinder unbinder;

    private List<Wallpaper> wallpapers;
    private GridLayoutManager wallpapersLayoutManager;
    private WallpapersAdapter wallpapersAdapter;

    public CategoryWallpapersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param categoryKey The key of the category to display its wallpapers.
     * @return A new instance of fragment CategoryWallpapersFragment.
     */
    public static CategoryWallpapersFragment newInstance(String categoryKey) {
        CategoryWallpapersFragment fragment = new CategoryWallpapersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_KEY, categoryKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryKey = getArguments().getString(ARG_CATEGORY_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_category_wallpapers, container
                , false);
        unbinder = ButterKnife.bind(this, fragmentView);

        setupWallpapersRecyclerView();

        return fragmentView;
    }

    private void setupWallpapersRecyclerView() {
        wallpapersRecyclerView.setHasFixedSize(true);

        wallpapersLayoutManager = new GridLayoutManager(getContext(), gridSpanCount);
        wallpapersRecyclerView.setLayoutManager(wallpapersLayoutManager);

        wallpapers = new ArrayList<>();
        wallpapersAdapter = new WallpapersAdapter(getContext(),wallpapers, this);
        wallpapersRecyclerView.setAdapter(wallpapersAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WallpaperCallbacks) {
            wallpaperCallbacks = (WallpaperCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WallpaperCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        wallpaperCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_WALLPAPERS, Parcels.wrap(wallpapers));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_WALLPAPERS)) {
            /* Fragment is being restored from previous state */
            Timber.d("Restoring wallpapers list from previous state");
            wallpapers = Parcels.unwrap(savedInstanceState.getParcelable(STATE_WALLPAPERS));
            wallpapersAdapter.swapWallpapers(wallpapers);

            // hide the 'loadingProgressBar'
            loadingProgressBar.setVisibility(View.GONE);
        }
        else {
            /* No previous state. Load wallpapers from Firebase */
            if (categoryKey.equals(getString(R.string.all_wallpapers_category_key))) {

                loadAllWallpapers();
            }
            else {

                loadSingleCategoryWallpapers();
            }
        }

    }

    /**
     * Get 'categoryKey' category wallpapers from Firebase.
     */
    private void loadSingleCategoryWallpapers() {
        DatabaseReference specifiedCategoryWallpapersDbRef = FirebaseDatabase.getInstance().getReference().child(
                FirebaseDatabaseContract.CategoryWallpapers.KEY + '/' + categoryKey);
        specifiedCategoryWallpapersDbRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot wallpaperKeySnapshot: dataSnapshot.getChildren()) {
                    final String wallpaperKey = wallpaperKeySnapshot.getKey();

                    DatabaseReference wallpaperDbRef = FirebaseDatabase.getInstance().getReference().child(
                            FirebaseDatabaseContract.Wallpapers.KEY + '/' + wallpaperKey);
                    wallpaperDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Wallpaper w = dataSnapshot.getValue(Wallpaper.class);
                            wallpapersAdapter.addWallpaper(w);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Timber.w(databaseError.toException()
                                    , "Failed loading wallpaper: '"+ wallpaperKey +"' from Firebase!");
                        }
                    });

                }

                // hide the 'loadingProgressBar'
                loadingProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.w(databaseError.toException()
                        , "Failed loading the category wallpapers data from Firebase!");

                // hide the 'loadingProgressBar'
                loadingProgressBar.setVisibility(View.GONE);

                Toast.makeText(getContext(), R.string.error_loading_category_wallpapers
                        , Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * Get all wallpapers from Firebase.
     */
    private void loadAllWallpapers() {
        DatabaseReference allWallpapersDbRef = FirebaseDatabase.getInstance().getReference().child(
                FirebaseDatabaseContract.Wallpapers.KEY);
        allWallpapersDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot wallpaperSnapshot: dataSnapshot.getChildren()) {
                    Wallpaper w = wallpaperSnapshot.getValue(Wallpaper.class);
                    wallpapersAdapter.addWallpaper(w);
                }

                // hide the 'loadingProgressBar'
                loadingProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.w(databaseError.toException()
                        , "Failed loading wallpapers data (category 'all') from Firebase!");

                // hide the 'loadingProgressBar'
                loadingProgressBar.setVisibility(View.GONE);

                Toast.makeText(getContext(), R.string.error_loading_category_wallpapers
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWallpaperItemClicked(int clickedWallpaperIndex) {
        wallpaperCallbacks.showWallpaperDetails(wallpapers.get(clickedWallpaperIndex));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface WallpaperCallbacks {
        void showWallpaperDetails(Wallpaper wallpaper);
    }

}
