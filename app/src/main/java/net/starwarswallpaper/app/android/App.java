package net.starwarswallpaper.app.android;

import android.app.Application;

import net.starwarswallpaper.app.android.utils.log.TimberReleaseTree;
import com.google.firebase.database.FirebaseDatabase;

import timber.log.Timber;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        else {
            Timber.plant(new TimberReleaseTree());
        }

        // Enable Firebase Realtime Database disk persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
