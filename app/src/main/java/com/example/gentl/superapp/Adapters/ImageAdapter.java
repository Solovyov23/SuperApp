package com.example.gentl.superapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Gentl on 3/22/2018.
 * Images for GridView
 */

public class ImageAdapter extends BaseAdapter 
{
    private static final int PADDING = 8;
    private static final int WIDTH = 250;
    private static final int HEIGHT = 250;
    private Context mContext;
    private List<String> mThumbIds;

    public ImageAdapter(Context c, List<String> ids)
	{
        mContext = c;
        this.mThumbIds = ids;
    }

    @Override
    public int getCount() 
	{
        return mThumbIds.size();
    }

    @Override
    public Object getItem(int position) 
	{
        return null;
    }

    // Will get called to provide the ID that
    // is passed to OnItemClickListener.onItemClick()
    @Override
    public long getItemId(int position)
	{
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
	{
        ImageView imageView = (ImageView) convertView;

        // if convertView's not recycled, initialize some attributes
        if (imageView == null) 
		{
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(WIDTH, HEIGHT));
            imageView.setPadding(PADDING, PADDING, PADDING, PADDING);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mThumbIds.get(position), options);

        // Set inSampleSize
        options.inSampleSize = 4;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap myBitmap = BitmapFactory.decodeFile(mThumbIds.get(position), options);

        imageView.setImageBitmap(myBitmap);

        return imageView;
    }
}