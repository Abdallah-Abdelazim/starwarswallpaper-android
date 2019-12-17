package com.tumblr.starwarswallpaper.models;

import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

@Parcel
@IgnoreExtraProperties
public class Author {

    String name;
    String referenceUrl;

    public Author() {
    }

    public Author(String name, String referenceUrl) {
        this.name = name;
        this.referenceUrl = referenceUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceUrl() {
        return referenceUrl;
    }

    public void setReferenceUrl(String referenceUrl) {
        this.referenceUrl = referenceUrl;
    }

    public boolean hasReferenceUrl() {
        return referenceUrl != null;
    }

    @Override
    public String toString() {
        return "Author{" +
                "name='" + name + '\'' +
                ", referenceUrl='" + referenceUrl + '\'' +
                '}';
    }

}
