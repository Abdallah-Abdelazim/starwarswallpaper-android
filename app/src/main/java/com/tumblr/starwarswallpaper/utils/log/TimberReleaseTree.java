package com.tumblr.starwarswallpaper.utils.log;

import android.util.Log;

import timber.log.Timber;

public class TimberReleaseTree extends Timber.DebugTree {

    @Override
    protected boolean isLoggable(String tag, int priority) {
        // Don't log VERBOSE, DEBUG and INFO
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return false;
        }

        // Log only ERROR, WARN and WTF
        return true;
    }
}