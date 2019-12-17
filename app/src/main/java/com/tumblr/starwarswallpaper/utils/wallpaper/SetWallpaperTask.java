package com.tumblr.starwarswallpaper.utils.wallpaper;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.tumblr.starwarswallpaper.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import timber.log.Timber;

/**
 * An async task used to change the device wallpaper (home screen, lock screen or both).
 * Setting lock screen wallpaper or both requires API Level 24 or higher.
 * @author Abdallah Abdelazim
 */
public class SetWallpaperTask extends AsyncTask<Bitmap, Void, Void> {

    public static final int BOTH = 0;
    public static final int HOME_SCREEN = 1;
    public static final int LOCK_SCREEN = 2;


    private final Context ctx;
    private int which;
    private TaskListener taskListener;

    /**
     * Provide required arguments for the task to run.
     * @param context Activity or service context.
     * @param which one of the constants BOTH, HOME_SCREEN, LOCK_SCREEN.
     */
    public SetWallpaperTask(@NonNull @NotNull Context context, int which
            , TaskListener listener) {

        this.ctx = context.getApplicationContext();
        this.which = which;
        this.taskListener = listener;
    }

    @Override
    protected void onPreExecute() {
        taskListener.onSetWallpaperTaskStarting();
    }

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        if (bitmaps.length > 0) {

            WallpaperManager wallpaperManager
                    = WallpaperManager.getInstance(ctx);

            switch (which) {
                case BOTH:
                    try {
                        wallpaperManager.setBitmap(bitmaps[0]); // On success, the intent Intent.ACTION_WALLPAPER_CHANGED is broadcast.
                    } catch (IOException e) {
                        Timber.e(e);
                        Toast.makeText(ctx, R.string.general_error, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case HOME_SCREEN:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            wallpaperManager.setBitmap(bitmaps[0], null, true
                                    , WallpaperManager.FLAG_SYSTEM);
                        } catch (IOException e) {
                            Timber.e(e);
                            Toast.makeText(ctx, R.string.general_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case LOCK_SCREEN:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            wallpaperManager.setBitmap(bitmaps[0], null, true
                                    , WallpaperManager.FLAG_LOCK);
                        } catch (IOException e) {
                            Timber.e(e);
                            Toast.makeText(ctx, R.string.general_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException(
                            "You must pass either BOTH, HOME_SCREEN or LOCK_SCREEN as an argument to 'which'.");
            }
        }
        else {
            throw new IllegalArgumentException("You must specify a bitmap as an argument.");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskListener.onSetWallpaperTaskFinished();
    }

    public interface TaskListener {

        /**
         * Callback method called just before the task has started execution.
         */
        void onSetWallpaperTaskStarting();

        /**
         * Callback method called after the task has finished execution.
         * No guarantee that the task succeeded on setting the wallpaper or not.
         */
        void onSetWallpaperTaskFinished();

    }
}
