package com.icecoldedge.popmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PosterGridFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PosterGridFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PosterGridFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //private OnFragmentInteractionListener mListener;

    private final String LOG_TAG = PosterGridFragment.class.getSimpleName();
    public ArrayAdapter<String> mPosterAdapter;

    public PosterGridFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PosterGridFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PosterGridFragment newInstance(String param1, String param2) {
        PosterGridFragment fragment = new PosterGridFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshMovieData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Settings menu for poster grid sort
        //setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_poster_grid, container, false);

        ArrayList<String> dummyList = new ArrayList<String>();

        for (int i = 0; i < 10; i++) {
            dummyList.add(i, Integer.toString(i));
        }

        mPosterAdapter = new PicassoImageAdapter(getActivity(),
                R.layout.list_item_poster,
                new ArrayList<String>());

        GridView posterGrid = (GridView)rootView.findViewById(R.id.gridview_posters);
        posterGrid.setAdapter(mPosterAdapter);

        return rootView;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }

    public void refreshMovieData() {
        new FetchMoviesTask().execute("popular");
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {

        private final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/popular";
        private final String API_KEY_PARAM = "api_key";

        @Override
        protected void onPostExecute(String[] movieData) {
            super.onPostExecute(movieData);

            mPosterAdapter.clear();

            if (movieData != null) {
                for (String s : movieData) {
                    mPosterAdapter.add(s);
                }
            }
        }

        @Override
        protected String[] doInBackground(String... searchType) {
//            if (searchType.length == 0)
//                return null;

            String[] movieData = null;
            String movieJsonStr = null;

            if(!isOnline())
                return movieData;

            movieJsonStr = FetchMovieDataFromAPI("popular");

            if (movieJsonStr != null) {
                Log.v(LOG_TAG, movieJsonStr);
            }

            // TODO: Parse JSON from API Call

            try {
                movieData = getPosterUrlsFromJSON(movieJsonStr);
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
            //TODO: Switch API based on searchType
            Uri movieUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
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

        private String[] getPosterUrlsFromJSON(String movieJsonStr)
            throws JSONException {

            final String MDB_RESULTS = "results";
            final String MDB_POSTER_PATH = "poster_path";
            final int NUM_POSTERS = 16;

            String[] resultStrs = new String[NUM_POSTERS];
            if (movieJsonStr == null) {
                Log.v(LOG_TAG, "Empty JSON String");
            }

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);

            for (int i = 0; i < movieArray.length() && i < NUM_POSTERS; i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                resultStrs[i] = Movie.POSTER_BASE_URL + Movie.POSTER_SIZE + movie.getString(MDB_POSTER_PATH);
            }

            return resultStrs;
        }
    }
}
