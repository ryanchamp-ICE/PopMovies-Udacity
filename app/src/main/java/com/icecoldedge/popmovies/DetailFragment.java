package com.icecoldedge.popmovies;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    public Movie mMovie;

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mMovie = (Movie)intent.getParcelableExtra("movie");

            if (mMovie != null) {
                ImageView imageView = (ImageView)rootView.findViewById(R.id.detail_poster_thumbnail);
                Picasso.with(getContext()).load(mMovie.getPosterPath()).into(imageView);

                TextView textView = (TextView)rootView.findViewById(R.id.detail_title_textview);
                textView.setText(mMovie.getTitle());

                textView = (TextView)rootView.findViewById(R.id.detail_sysnopsis_textview);
                textView.setText(mMovie.getSynopsis());

                textView = (TextView)rootView.findViewById(R.id.detail_rating_textview);
                textView.setText("Rating: " + String.valueOf(mMovie.getRating()));

                textView = (TextView)rootView.findViewById(R.id.detail_release_date_textview);
                DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                String date = dateFormat.format(mMovie.getReleaseDate());
                textView.setText("Release date: " + date);
            }
        }

        return rootView;
    }

}
