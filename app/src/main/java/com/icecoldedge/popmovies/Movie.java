package com.icecoldedge.popmovies;

import java.util.Date;

/**
 * Created by rchamp on 8/8/2016.
 */
public class Movie {
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
}
