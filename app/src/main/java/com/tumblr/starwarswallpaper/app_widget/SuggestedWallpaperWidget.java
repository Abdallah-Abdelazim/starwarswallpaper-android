package com.tumblr.starwarswallpaper.app_widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.tumblr.starwarswallpaper.R;
import com.tumblr.starwarswallpaper.activities.WallpaperDetailsActivity;
import com.tumblr.starwarswallpaper.data.FirebaseDatabaseContract;
import com.tumblr.starwarswallpaper.models.Wallpaper;
import com.tumblr.starwarswallpaper.utils.glide.GlideApp;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import timber.log.Timber;

/**
 * Implementation of the Suggested Wallpaper App Widget functionality.
 */
public class SuggestedWallpaperWidget extends AppWidgetProvider {

    public static void updateAppWidgets(final Context context) {

        // Construct the RemoteViews object
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName()
                , R.layout.suggested_wallpaper_widget);

        // Load the suggested wallpaper asynchronously
        DatabaseReference suggestedWallpaperDbRef = FirebaseDatabase.getInstance().getReference().child(
                FirebaseDatabaseContract.SuggestedWallpaper.KEY);
        suggestedWallpaperDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot suggestedWallpaperDataSnapshot =
                        dataSnapshot.getChildren().iterator().next();
                final String suggestedWallpaperKey = suggestedWallpaperDataSnapshot.getKey();
                Timber.d("Wallpaper key = " + suggestedWallpaperKey);

                DatabaseReference suggestedWallpaperDbRef = FirebaseDatabase.getInstance().getReference().child(
                        FirebaseDatabaseContract.Wallpapers.KEY + '/' + suggestedWallpaperKey);
                suggestedWallpaperDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Wallpaper suggestedWallpaper = dataSnapshot.getValue(Wallpaper.class);
                        Timber.d("Suggested wallpaper: " + suggestedWallpaper);

                        if (suggestedWallpaper != null) {
                            setupSuggestedWallpaperAppWidgets(context, remoteViews, suggestedWallpaper);
                        }
                        else {
                            Timber.e("Error: suggestedWallpaper is null");

                            Toast.makeText(context, R.string.error_loading_appwidget
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Timber.e(databaseError.toException()
                                , "Failed loading suggested wallpaper: '"+ suggestedWallpaperKey
                                        +"' from Firebase!");

                        Toast.makeText(context, R.string.error_loading_appwidget
                                , Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.e(databaseError.toException()
                        , "Failed loading the suggested wallpaper key from Firebase!");

                Toast.makeText(context, R.string.error_loading_appwidget
                        , Toast.LENGTH_SHORT).show();
            }
        });

        pushWidgetUpdate(context, remoteViews);
    }

    private static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        // Instruct the widget manager to update the widget
        ComponentName thisWidget = new ComponentName(context, SuggestedWallpaperWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);
    }

    private static void setupSuggestedWallpaperAppWidgets(Context context, final RemoteViews remoteViews
            , Wallpaper suggestedWallpaper) {

        // Display the suggested wallpaper title
        remoteViews.setTextViewText(R.id.tv_wallpaper_title, suggestedWallpaper.getTitle());

        // Display the wallpaper thumbnail
        StorageReference suggestedWallpaperStorageRef = FirebaseStorage.getInstance()
                .getReference(suggestedWallpaper.getThumbnailPath());
        ComponentName thisWidget = new ComponentName(context, SuggestedWallpaperWidget.class);
        AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.iv_wallpaper
                , remoteViews, thisWidget);
        GlideApp.with(context)
                .asBitmap()
                .load(suggestedWallpaperStorageRef)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(appWidgetTarget);

        // Create an Intent to launch WallpaperDetailsActivity
        Intent intent = WallpaperDetailsActivity.createStartIntent(context, suggestedWallpaper);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.iv_wallpaper, pendingIntent);

        pushWidgetUpdate(context, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        updateAppWidgets(context);
    }

    @Override
    public void onEnabled(Context context) {
        // relevant functionality for when the first widget is created
        // Show a toast to introduce the user to the functionality of the app widget.
        Toast.makeText(context, R.string.suggested_wallpaper_appwidget_greeter
                , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisabled(Context context) {
        // relevant functionality for when the last widget is disabled
    }
}

