package com.example.gentl.superapp.Adapters;


import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.gentl.superapp.DataClasses.City;
import com.example.gentl.superapp.DataClasses.GeonamesDB;
import com.example.gentl.superapp.R;

/**
 * Created by Gentl on 3/21/2018.
 * Adapter is used for data exchange between the city database
 * and the list view that displays these cities
 */

public class CitiesListViewAdapter extends CursorAdapter implements LoaderManager.LoaderCallbacks<Cursor>
{
    private Activity context;
    private int loaderId = 1;

    public CitiesListViewAdapter(Activity context, Cursor cursor)
    {
        super(context, cursor);
        this.context = context;
        // For background loading data
        this.context.getLoaderManager().restartLoader(loaderId, new Bundle(), this);
    }

    public int getCount()
    {
        return getCursor().getCount();
    }

    public void addCity(City city)
    {
        loaderGetDatabase().addCity(city);
        changeCursor();
    }

    public boolean removeCity(int idCity)
    {
        boolean res = loaderGetDatabase().removeCity(idCity);
        changeCursor();
        return res;
    }

    public void changeCursor()
    {
        try
        {
            // For background loading data
            context.getLoaderManager().getLoader(loaderId).forceLoad();
        }
        catch (Exception e)
        {
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void notifyDataSetChanged()
    {
        super.notifyDataSetChanged();
    }

    // get City From Position
    public City getCityFromPosition(int position)
    {
        Cursor cursor = getCursor();
        City city = null;
        if(cursor.moveToPosition(position))
		{
            city = new City();
            city.setIdCity(cursor.getInt(cursor.getColumnIndex(loaderGetDatabase().CITY_ID)));
            city.setTitleCity(cursor.getString(cursor.getColumnIndex(loaderGetDatabase().CITY_TITLE)));
        }
        return city;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) 
	{
        return LayoutInflater.from(context).inflate(R.layout.city_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        final City city = getCityFromPosition(cursor.getPosition());

        TextView tvIdCity = view.findViewById(R.id.tvIdCity);
        TextView tvTitleCity = view.findViewById(R.id.tvTitleCity);

        tvIdCity.setText(Integer.toString(city.getIdCity()));
        tvTitleCity.setText(String.valueOf(cursor.getPosition()+1 + ") " + city.getTitleCity()));
    }

    //------------------------------------------------For background loading----------------------------

    private WordCursorLoader loader;
    // ---------------loader listener-------------
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
        loader = new WordCursorLoader(context);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if(data == null) return;
        changeCursor(data);
        this.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        changeCursor(null);
        this.notifyDataSetChanged();
    }
    public GeonamesDB loaderGetDatabase()
    {
        return loader.getDatabase();
    }
}

/*
* Loads cities from DB asynchronously
*/
class WordCursorLoader extends CursorLoader 
{
    private Context context;

    public WordCursorLoader(Context context) 
	{
        super(context);
    }

    public GeonamesDB getDatabase()
    {
        return GeonamesDB.getInstance(context);
    }

    //For background loading cities
    @Override
    public Cursor loadInBackground() 
	{
        Cursor cursor = getDatabase().getAllCities();
        return cursor;
    }
}
