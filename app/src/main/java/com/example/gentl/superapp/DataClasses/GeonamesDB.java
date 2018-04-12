package com.example.gentl.superapp.DataClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Gentl on 3/21/2018.
 * DB related operations.
 */
public class GeonamesDB extends SQLiteOpenHelper 
{
    final static String databaseName = "geonames.s3db";
    static String folderPath = "";
    final static int databaseVersion = 1;

    // KEYS
    public static final String KEY_ROWID = "rowid _id";
    private static final String CITIES_TABLE = "Cities";
    public static final String CITY_ID = "id_city";
    public static final String CITY_TITLE = "title";

	// Absolute path to db
    String outFullDatabaseFileName;

    Context appContext;

	// Single instance of the DB
    private static GeonamesDB mInstance;
    private SQLiteDatabase db;

    // Return a single instance of the database
    public static GeonamesDB getInstance(Context context) 
	{
        if (mInstance == null) 
		{
            mInstance = new GeonamesDB(context);
        }
        return mInstance;
    }

    // Create DB
    private GeonamesDB(Context context) 
	{
        super(context, databaseName, null, databaseVersion);
        appContext = context;
        folderPath = appContext.getFilesDir().getPath();
        try 
		{
            // copying from the assets folder to the folder for work
            copyDataBase(databaseName);
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
        }
    }

    // copying from the assets folder to the folder for work
    private void copyDataBase(String dbname) throws IOException 
	{
        // open the asset database
        InputStream myInput = appContext.getAssets().open(dbname);

        File db_folder = new File(folderPath);
        if (!db_folder.exists())
            db_folder.mkdir();

        outFullDatabaseFileName = folderPath + "/" + dbname;

        if (new File(outFullDatabaseFileName).exists() && new File(outFullDatabaseFileName).length() != 0) 
		{
            openDataBase(outFullDatabaseFileName);
        } 
		else 
		{
            // path to the database in the databases folder
            File file = new File(outFullDatabaseFileName);
            if (!file.exists()) file.createNewFile();

            OutputStream myOutput = new FileOutputStream(outFullDatabaseFileName);
            byte[] buffer = new byte[102400];
            int length;
            while ((length = myInput.read(buffer)) > 0) 
			{
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
            openDataBase(outFullDatabaseFileName);
        }
    }

    public boolean isOpen() 
	{
        return db != null && db.isOpen();
    }

    public void close() 
	{
        if (isOpen())
		{
            //db.close();
        }
    }

    private void openDataBase(String path) 
	{
        if (!isOpen()) 
		{
            db = SQLiteDatabase.openDatabase(path, null, 0);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) 
	{

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) 
	{

    }

    // Return all cities from the database
    public Cursor getAllCities() 
	{
        Cursor cursor = db.query(CITIES_TABLE, new String[]{KEY_ROWID, CITY_ID, CITY_TITLE},
                null, null, null, null, CITY_TITLE + " ASC"); //DESC

        // move the cursor to the first line of the query result
        if (cursor != null) cursor.moveToFirst();
        return cursor;
    }

    // Add a new city
    public long addCity(City city) 
	{
        ContentValues initialValues = new ContentValues();
        initialValues.put(CITY_TITLE, city.getTitleCity());

        return db.insert(CITIES_TABLE, null, initialValues);
    }

    // Delete city by Id
    public boolean  removeCity(int cityId) 
	{
        return db.delete(CITIES_TABLE, CITY_ID + "=" + cityId, null) > 0;
    }
}
