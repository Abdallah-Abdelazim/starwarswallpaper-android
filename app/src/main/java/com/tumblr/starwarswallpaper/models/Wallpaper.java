package com.tumblr.starwarswallpaper.models;

import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

@Parcel
@IgnoreExtraProperties
public class Wallpaper {

    String title;
    String description;
    String thumbnailPath;
    String wallpaperPath;
    String authorKey;

    Author author;

    public Wallpaper() {
    }

    public Wallpaper(String title, String description, String thumbnailPath, String wallpaperPath
            , String authorKey, Author author) {
        this.title = title;
        this.description = description;
        this.thumbnailPath = thumbnailPath;
        this.wallpaperPath = wallpaperPath;
        this.authorKey = authorKey;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getWallpaperPath() {
        return wallpaperPath;
    }

    public void setWallpaperPath(String wallpaperPath) {
        this.wallpaperPath = wallpaperPath;
    }

    public String getAuthorKey() {
        return authorKey;
    }

    public void setAuthorKey(String authorKey) {
        this.authorKey = authorKey;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public boolean hasAuthor() {
        return authorKey != null;
    }

    @Override
    public String toString() {
        return "Wallpaper{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", wallpaperPath='" + wallpaperPath + '\'' +
                ", authorKey='" + authorKey + '\'' +
                ", author=" + author +
                '}';
    }

}
