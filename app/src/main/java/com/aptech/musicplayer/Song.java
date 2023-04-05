package com.aptech.musicplayer;

import android.net.Uri;

public class Song {

    private String title;
    private String artist;
    private Uri uri;
    private Uri artworkUri;
    int duration;
    int size;

    public Song(){}

    public Song(String title, Uri uri, Uri artworkUri, int duration, int size,String artist) {
        this.title = title;
        this.uri = uri;
        this.artworkUri = artworkUri;
        this.duration = duration;
        this.size = size;
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public void setArtworkUri(Uri artworkUri) {
        this.artworkUri = artworkUri;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
