package net.starwarswallpaper.app.android.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.starwarswallpaper.app.android.R;
import net.starwarswallpaper.app.android.data.FirebaseDatabaseContract;
import net.starwarswallpaper.app.android.fragments.SettingWallpaperDialogFragment;
import net.starwarswallpaper.app.android.models.Author;
import net.starwarswallpaper.app.android.models.Wallpaper;
import net.starwarswallpaper.app.android.utils.glide.GlideApp;
import net.starwarswallpaper.app.android.utils.wallpaper.SetWallpaperTask;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class WallpaperDetailsActivity extends AppCompatActivity implements SetWallpaperTask.TaskListener {

    public static final String EXTRA_WALLPAPER = "com.abdallah.starwarswallpaper.EXTRA_WALLPAPER";
    private static final String TAG_SETTING_WALLPAPER_DIALOG_FRAGMENT = "TAG_SETTING_WALLPAPER_DIALOG_FRAGMENT";
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 0;

    @BindView(R.id.pv_wallpaper) PhotoView wallpaperPhotoView;
    @BindView(R.id.pb_wallpaper) ProgressBar loadingProgressBar;
    @BindView(R.id.ll_wallpaper_info) LinearLayout wallpaperInfoLinearLayout;
    @BindView(R.id.tv_wallpaper_description) TextView wallpaperDescriptionTextView;
    @BindView(R.id.tv_author_link) TextView authorLinkTextView;

    private boolean isWallpaperInfoVisible = true;

    private Wallpaper wallpaper;
    private StorageReference wallpaperStorageRef;
    private SettingWallpaperDialogFragment settingWallpaperDialogFragment;
    private DownloadManager downloadManager;
    private long downloadRefId;

    public static Intent createStartIntent(Context context, Wallpaper wallpaper) {
        Intent intent = new Intent(context, WallpaperDetailsActivity.class);
        intent.putExtra(EXTRA_WALLPAPER, Parcels.wrap(wallpaper));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(EXTRA_WALLPAPER)) {
            wallpaper = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_WALLPAPER));
            Timber.d("wallpaper=" + wallpaper);
        }
        else {
            Timber.e("WallpaperDetailsActivity: No wallpaper was found in the intent extras!");
        }

        // set the title to be the wallpaper title
        setTitle(wallpaper.getTitle());

        // load the high quality wallpaper inside the PhotoView
        wallpaperStorageRef = FirebaseStorage.getInstance().getReference(wallpaper.getWallpaperPath());
        GlideApp.with(this)
                .load(wallpaperStorageRef)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model
                            , Target<Drawable> target, boolean isFirstResource) {
                        Timber.e(e, "Failed loading wallpaper");
                        loadingProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model
                            , Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Timber.d("Finished loading wallpaper");
                        loadingProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(wallpaperPhotoView);

        // display the wallpaper description
        wallpaperDescriptionTextView.setText(wallpaper.getDescription());

        // display author info
        if (savedInstanceState == null) {
            // get the wallpaper author data from  Firebase
            DatabaseReference wallpaperAuthorDbRef = FirebaseDatabase.getInstance().getReference().child(
                    FirebaseDatabaseContract.Authors.KEY + '/' + wallpaper.getAuthorKey());
            wallpaperAuthorDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Author author = dataSnapshot.getValue(Author.class);
                    wallpaper.setAuthor(author);

                    displayAuthorLink();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Timber.e(databaseError.toException(), "Failed loading the wallpaper author data");

                    Toast.makeText(WallpaperDetailsActivity.this, R.string.error_loading_author
                            , Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            displayAuthorLink();
        }

        // add listener on the 'wallpaperPhotoView' to show/hide the 'll_wallpaper_info' on click
        wallpaperPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWallpaperInfoVisible) {
                    wallpaperInfoLinearLayout.setVisibility(View.GONE);
                    isWallpaperInfoVisible = false;
                }
                else {
                    wallpaperInfoLinearLayout.setVisibility(View.VISIBLE);
                    isWallpaperInfoVisible = true;
                }
            }
        });

        // initialize the download manager
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

    }

    private void displayAuthorLink() {
        if (wallpaper.getAuthor() != null) {
            authorLinkTextView.setText(Html.fromHtml(String.format("<a href=\"%2$s\">%1$s</a>"
                    , wallpaper.getAuthor().getName(), wallpaper.getAuthor().getReferenceUrl())));
            authorLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wallpaper_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_set_wallpaper:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // TODO display a dialog to let the user choose where to set the wallpaper
                    // temporary it just sets the home screen
                    setHomeScreenWallpaper();
                }
                else {
                    // Only setting the home screen is available. Other options are not supported.
                    setHomeScreenWallpaper();
                }
                return true;
            case R.id.action_download_wallpaper:
                downloadWallpaper();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void downloadWallpaper() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        wallpaperStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Timber.d("Wallpaper download URI: " + uri);
                loadingProgressBar.setVisibility(View.GONE);

                if (ContextCompat.checkSelfPermission(WallpaperDetailsActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(WallpaperDetailsActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // Show an explanation to the user
                        Toast.makeText(WallpaperDetailsActivity.this, R.string.explain_write_external_storage_permission
                                , Toast.LENGTH_LONG).show();
                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(WallpaperDetailsActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                    }
                } else {
                    // Permission has already been granted

                    String wallpaperFileName = getString(R.string.wallpaper_downloaded_file_name
                            , wallpaper.getTitle(), wallpaper.getAuthor().getName());
                    DownloadManager.Request downloadRequest = new DownloadManager.Request(uri);
                    downloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE);
                    downloadRequest.setAllowedOverRoaming(false);
                    downloadRequest.setTitle(getString(R.string.wallpaper_download_notification_title
                            , wallpaperFileName));
                    downloadRequest.setDescription(getString(R.string.wallpaper_download_notification_description
                                    , wallpaperFileName));
                    downloadRequest.allowScanningByMediaScanner();
                    downloadRequest.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    downloadRequest.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, getString(
                                    R.string.wallpaper_downloaded_file_path, wallpaperFileName));

                    downloadRefId = downloadManager.enqueue(downloadRequest);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.e(e, "Failed creating the download url for the wallpaper");
                loadingProgressBar.setVisibility(View.GONE);

                Toast.makeText(WallpaperDetailsActivity.this, R.string.error_download_wallpaper
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[]
            , @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    downloadWallpaper();
                } else {
                    Toast.makeText(this, R.string.error_download_wallpaper_permission_missing
                            , Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void setHomeScreenWallpaper() {

        GlideApp.with(this)
                .asBitmap()
                .load(wallpaperStorageRef)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource
                            , @Nullable Transition<? super Bitmap> transition) {
                        new SetWallpaperTask(WallpaperDetailsActivity.this
                                , SetWallpaperTask.HOME_SCREEN, WallpaperDetailsActivity.this)
                                .execute(resource);
                    }
                });

    }

    /**
     * Requires API Level 24 or higher.
     */
    private void setLockScreenWallpaper() {

        GlideApp.with(this)
                .asBitmap()
                .load(wallpaperStorageRef)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource
                            , @Nullable Transition<? super Bitmap> transition) {
                        new SetWallpaperTask(WallpaperDetailsActivity.this
                                , SetWallpaperTask.LOCK_SCREEN, WallpaperDetailsActivity.this)
                                .execute(resource);
                    }
                });

    }

    /**
     * Requires API Level 24 or higher.
     */
    private void setBothWallpaper() {

        GlideApp.with(this)
                .asBitmap()
                .load(wallpaperStorageRef)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource
                            , @Nullable Transition<? super Bitmap> transition) {
                        new SetWallpaperTask(WallpaperDetailsActivity.this
                                , SetWallpaperTask.BOTH, WallpaperDetailsActivity.this)
                                .execute(resource);
                    }
                });

    }

    @Override
    public void onSetWallpaperTaskStarting() {
        if (settingWallpaperDialogFragment == null) {
            settingWallpaperDialogFragment = new SettingWallpaperDialogFragment();
        }
        settingWallpaperDialogFragment.show(getSupportFragmentManager(), TAG_SETTING_WALLPAPER_DIALOG_FRAGMENT);
    }

    @Override
    public void onSetWallpaperTaskFinished() {
        settingWallpaperDialogFragment.dismiss();
    }
}
