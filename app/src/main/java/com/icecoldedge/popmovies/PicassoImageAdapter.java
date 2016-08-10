package com.icecoldedge.popmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rchamp on 8/8/2016.
 */
public class PicassoImageAdapter extends ArrayAdapter<Movie> {
    private Context mContext;

    public PicassoImageAdapter(Context c, int resourceID, List<Movie> movieList) {
        super(c, resourceID, movieList);
        mContext = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        Movie movie = (Movie)getItem(position);

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_poster, null);
        }

        imageView = (ImageView)convertView.findViewById(R.id.list_item_poster_imageview);

        Picasso.with(mContext).load(movie.getPosterPath()).into(imageView);

        return imageView;
    }
}
