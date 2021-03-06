package com.icecoldedge.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;


public class PosterGridFragment extends Fragment {

    private final String LOG_TAG = PosterGridFragment.class.getSimpleName();
    private ArrayAdapter<Movie> mPosterAdapter;

    private ArrayList<Movie> mMovieList;

    public PosterGridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshMovieData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            mMovieList = new ArrayList<Movie>();
        }
        else {
            mMovieList = savedInstanceState.getParcelableArrayList("movies");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", mMovieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_poster_grid, container, false);

        mPosterAdapter = new PicassoImageAdapter(getActivity(),
                R.layout.list_item_poster,
                new ArrayList<Movie>());

        GridView posterGrid = (GridView)rootView.findViewById(R.id.gridview_posters);
        posterGrid.setAdapter(mPosterAdapter);

        posterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Movie movie = (Movie)adapterView.getItemAtPosition(i);
                if (movie != null) {
                    Context context = getActivity().getApplicationContext();
                    Intent detailIntent = new Intent(context, DetailActivity.class);
                    detailIntent.putExtra("movie", movie);
                    startActivity(detailIntent);
                } else {
                    Log.v(LOG_TAG, "Movie data not found");
                }
            }
        });

        return rootView;
    }

    public void refreshMovieData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sortOrderKey = getString(R.string.pref_sort_order_key);
        String defaultSort = getString(R.string.pref_sort_order_default);

        new FetchMoviesTask().execute(prefs.getString(sortOrderKey, defaultSort));
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
        private final String API_KEY_PARAM = "api_key";

        @Override
        protected void onPostExecute(ArrayList<Movie> movieData) {
            super.onPostExecute(movieData);

            if (movieData != null) {
                mPosterAdapter.clear();
                mMovieList = movieData;

                mPosterAdapter.addAll(mMovieList);
            }
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... searchType) {
            if (searchType.length == 0)
                return null;

            ArrayList<Movie> movieData = null;
            String movieJsonStr = null;

            if(!isOnline())
                return movieData;

            movieJsonStr = FetchMovieDataFromAPI(searchType[0]);

            if (movieJsonStr != null) {
                Log.v(LOG_TAG, movieJsonStr);
            }

            // TODO: Parse JSON from API Call
            try {
                movieData = getMoviesFromJSON(movieJsonStr);
            }
            catch(JSONException e) {
                Log.e(LOG_TAG, "Error", e);
                movieData = null;
            }

            return movieData;
        }

        private boolean isOnline() {
            ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }

        private String BuildMovieUrl(String searchType) {
            Uri movieUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(searchType)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            return movieUri.toString();
        }

        private String FetchMovieDataFromAPI(String searchType) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;
            try {
                URL url = new URL(BuildMovieUrl(searchType));

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    movieJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    movieJsonStr = null;
                }

                movieJsonStr = buffer.toString();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Error ", e);
                movieJsonStr = null;
            }
            finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }

                if(reader != null) {
                    try {
                        reader.close();
                    }
                    catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return movieJsonStr;
        }

        private ArrayList<Movie> getMoviesFromJSON(String movieJsonStr)
            throws JSONException {

            final String MDB_RESULTS = "results";
            final String MDB_ID = "id";
            final String MDB_TITLE = "original_title";
            final String MDB_POSTER_PATH = "poster_path";
            final String MDB_SYNOPSIS = "overview";
            final String MDB_RATING = "vote_average";
            final String MDB_RELEASE_DATE = "release_date";
            final int NUM_POSTERS = 16;

            ArrayList<Movie> results = new ArrayList<Movie>();
            if (movieJsonStr == null) {
                Log.v(LOG_TAG, "Empty JSON String");
            }

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                Movie result = new Movie();
                result.setMovieId(movie.getInt(MDB_ID));
                result.setTitle(movie.getString(MDB_TITLE));
                result.setPosterPath(Movie.POSTER_BASE_URL + Movie.POSTER_SIZE + movie.getString(MDB_POSTER_PATH));
                result.setSynopsis(movie.getString(MDB_SYNOPSIS));
                result.setRating((float)movie.getDouble(MDB_RATING));

                DateFormat format = new SimpleDateFormat("yyyy-mm-dd");

                try {
                    result.setReleaseDate(format.parse(movie.getString(MDB_RELEASE_DATE)));
                }
                catch (ParseException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    result.setReleaseDate(null);
                }

                results.add(result);
            }

            return results;
        }
    }
}
