package com.icecoldedge.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by rchamp on 8/8/2016.
 */
public class Movie implements Parcelable {
    public static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String POSTER_SIZE = "w185";

    private int id;
    private String title;
    private String posterPath;
    private String synopsis;
    private float rating;
    private Date releaseDate;

    public Movie(int id, String title, String posterPath, String synopsis, float rating, Date releaseDate) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.synopsis = synopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        posterPath = in.readString();
        synopsis = in.readString();
        rating = in.readFloat();
        releaseDate = new Date(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(posterPath);
        dest.writeString(synopsis);
        dest.writeFloat(rating);
        dest.writeLong(getReleaseDate().getTime());
    }

    Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };

    public int getMovieId() {
        return id;
    }

    public void setMovieId(int val) {
        id = val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String val) {
        posterPath = val;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String val) {
        synopsis = val;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float val) {
        rating = val;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
}
