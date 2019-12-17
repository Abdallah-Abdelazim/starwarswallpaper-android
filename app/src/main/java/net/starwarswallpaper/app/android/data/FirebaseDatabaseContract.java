package net.starwarswallpaper.app.android.data;

/**
 * 'Star Wars Wallpapers' NoSQL database key constants used in each node on Firebase Realtime Database.
 */
public final class FirebaseDatabaseContract {

    private FirebaseDatabaseContract() {/* prevent instantiation */}

    /**
     * 'categories' node.
     */
    public static class Categories {

        public static final String KEY = "categories";

        public static final String NAME_KEY = "name";
        public static final String DESCRIPTION_KEY = "description";

    }

    /**
     * 'authors' node.
     */
    public static class Authors {

        public static final String KEY = "authors";

        public static final String NAME_KEY = "name";
        public static final String REFERENCE_URL_KEY = "referenceUrl";

    }

    /**
     * 'wallpapers' node.
     */
    public static class Wallpapers {

        public static final String KEY = "wallpapers";

        public static final String TITLE_KEY = "title";
        public static final String DESCRIPTION_KEY = "description";
        public static final String THUMBNAIL_PATH_KEY = "thumbnailPath";
        public static final String WALLPAPER_PATH_KEY = "wallpaperPath";
        public static final String AUTHOR_KEY_KEY = "authorKey";

    }

    /**
     * 'categoryWallpapers' node.
     */
    public static class CategoryWallpapers {

        public static final String KEY = "categoryWallpapers";

    }

    /**
     * 'suggestedWallpaper' node.
     */
    public static class SuggestedWallpaper {

        public static final String KEY = "suggestedWallpaper";

    }

}
