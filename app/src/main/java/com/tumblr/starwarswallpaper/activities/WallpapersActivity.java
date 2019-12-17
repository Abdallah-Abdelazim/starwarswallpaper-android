package com.tumblr.starwarswallpaper.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tumblr.starwarswallpaper.R;
import com.tumblr.starwarswallpaper.adapters.CategoriesAdapter;
import com.tumblr.starwarswallpaper.data.FirebaseDatabaseContract;
import com.tumblr.starwarswallpaper.fragments.CategoryWallpapersFragment;
import com.tumblr.starwarswallpaper.models.Category;
import com.tumblr.starwarswallpaper.models.Wallpaper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class WallpapersActivity extends AppCompatActivity
        implements CategoriesAdapter.CategoryItemClickListener
        , CategoryWallpapersFragment.WallpaperCallbacks {

    private static final String STATE_ACTIVITY_TITLE = "STATE_ACTIVITY_TITLE";
    private static final String STATE_CURRENT_SELECTED_CATEGORY_POSITION = "STATE_CURRENT_SELECTED_CATEGORY_POSITION";

    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.rv_nav_categories) RecyclerView navCategoriesRecyclerView;
    @BindView(R.id.pb_nav_categories) ProgressBar navCategoriesProgressBar;

    private DatabaseReference categoriesDbRef;

    private List<Category> categories;
    private CategoriesAdapter categoriesAdapter;
    private int currentSelectedCategoryPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpapers);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            // open 'all' category (default category)
            setTitle(R.string.all_wallpapers_category_name);

            CategoryWallpapersFragment wallpapersFragment =
                    CategoryWallpapersFragment.newInstance(getString(R.string.all_wallpapers_category_key));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.wallpapers_fragment_container, wallpapersFragment)
                    .commit();
        }
        else {
            // restore activity title
            setTitle(savedInstanceState.getCharSequence(STATE_ACTIVITY_TITLE));
            // restore 'currentSelectedCategoryPosition' field
            currentSelectedCategoryPosition = savedInstanceState.getInt(STATE_CURRENT_SELECTED_CATEGORY_POSITION);
        }

        setupCategoriesRecyclerView();
        loadWallpaperCategories();
    }

    private void setupCategoriesRecyclerView() {
        navCategoriesRecyclerView.setHasFixedSize(false);
        navCategoriesRecyclerView.setNestedScrollingEnabled(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        navCategoriesRecyclerView.setLayoutManager(layoutManager);

        categoriesAdapter = new CategoriesAdapter(currentSelectedCategoryPosition, this);
        navCategoriesRecyclerView.setAdapter(categoriesAdapter);
    }

    private void loadWallpaperCategories() {
        categories = new ArrayList<>();

        // Add the 'All' category item
        categories.add(new Category(getString(R.string.all_wallpapers_category_key)
                , getString(R.string.all_wallpapers_category_name)
                , getString(R.string.all_wallpapers_category_description)));

        // Get the categories from Firebase
        categoriesDbRef = FirebaseDatabase.getInstance().getReference().child(
                FirebaseDatabaseContract.Categories.KEY);
        categoriesDbRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot categorySnapshot: dataSnapshot.getChildren()) {
                    Category c = categorySnapshot.getValue(Category.class);
                    if (c != null) {
                        c.setKey(categorySnapshot.getKey());
                        categories.add(c);
                    }
                }

                Timber.d("Categories fetched: " + categories);

                categoriesAdapter.swapCategoriesList(categories);

                // hide the 'navCategoriesProgressBar'
                navCategoriesProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.w(databaseError.toException()
                        , "Failed loading categories data from Firebase!");

                // hide the 'navCategoriesProgressBar'
                navCategoriesProgressBar.setVisibility(View.GONE);

                Toast.makeText(WallpapersActivity.this, R.string.error_loading_categories
                        , Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCategoryItemClicked(int clickedItemIndex) {
        Category clickedCategory = categories.get(clickedItemIndex);

        setTitle(clickedCategory.getName());

        CategoryWallpapersFragment wallpapersFragment =
                CategoryWallpapersFragment.newInstance(clickedCategory.getKey());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.wallpapers_fragment_container, wallpapersFragment)
                .commit();

        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void showWallpaperDetails(Wallpaper wallpaper) {
        Timber.d("Selected wallpaper: " + wallpaper);

        startActivity(WallpaperDetailsActivity.createStartIntent(this, wallpaper));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(STATE_ACTIVITY_TITLE, getTitle());
        outState.putInt(STATE_CURRENT_SELECTED_CATEGORY_POSITION, categoriesAdapter.getSelectedPosition());
    }

}
