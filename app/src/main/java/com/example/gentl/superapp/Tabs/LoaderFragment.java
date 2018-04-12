package com.example.gentl.superapp.Tabs;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.example.gentl.superapp.Adapters.ImageAdapter;
import com.example.gentl.superapp.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoaderFragment extends Fragment implements View.OnClickListener 
{
    // To fill the link, its text is used to further download the archive
    private EditText editTextURL;

    // Progress Dialog
    private ProgressDialog pDialog;
    private static final int progress_bar_type = 0;

    // To display pictures
    private GridView gridViewImages;
    // adapter for displaying images in gridViewImages
    private ImageAdapter imageAdapter;
    // list of image paths for the imageAdapter
    private ArrayList<String> images;
    // List of paths of all downloaded images from the archive, which we downloaded from the Internet
    private File[] listFile;

    // Store all the names of the archive folders, to read the pictures from all of them
    private List<String> foldersForScanning = new ArrayList<String>();


    public LoaderFragment() 
	{
        // Required empty public constructor
    }

    public static LoaderFragment newInstance() 
	{
        LoaderFragment fragment = new LoaderFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) 
	{
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_loader, container, false);

        // For manual insertion of URl from which you need to download the archive
        editTextURL = view.findViewById(R.id.editTextURL);

        // The button, upon clicking which the archive will downloaded
        Button bDownload = view.findViewById(R.id.bDownload);
        bDownload.setOnClickListener(this);

        // Just a list of image paths for the gridViewImages adapter
        images = new ArrayList<String>();// list of file paths
        imageAdapter = new ImageAdapter(getContext(), images);
        gridViewImages = view.findViewById(R.id.gridViewImages);
        gridViewImages.setAdapter(imageAdapter);

        // To display the progress of downloading the archive from the Internet
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.downloading_file_please_wait));
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(true);

        // Check for storage permissions
        // if not, then ask the user permission
        checkPermission();
        return view;
    }

    // Scan the folder in which to unpack the archive
    public void getFromSdcard()
    {
        // foldersForScanning - the number of folders that were in the archive.
        // Scan them all
        for(int i = 0; i <= foldersForScanning.size() - 1; i++) 
		{

            File file = new File(getContext()
                    .getApplicationContext()
                    .getFilesDir()
                    .getAbsolutePath()
                    + "/UploadedImages/"
                    + foldersForScanning.get(i) + "/");

            if (file.isDirectory()) 
			{
                listFile = file.listFiles();

                for (int k = 0; k < listFile.length; k++) 
				{
                    images.add(listFile[k].getAbsolutePath());
                }
                // Update the data in GridView, to display new images
                imageAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Start to display the progress of downloading the archive from the Internet
     * */
    public void showDialog(int progress)
    {
        pDialog.setProgress(0);
        pDialog.show();
    }

    // Close show download progress
    public void dismissDialog(int dismissDialog)
    {
        pDialog.dismiss();
    }

    public interface OnTaskCompleted
	{
        void onTaskCompleted();
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> 
	{

        private OnTaskCompleted listener;

        public DownloadFileFromURL(OnTaskCompleted listener)
		{
            this.listener=listener;
        }
        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() 
		{
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) 
		{
            int count;
            try 
			{
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // needed to show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();

                // If the link was broken
                if(lenghtOfFile == -1)
                {
                    errorUpload("Non-existing link!");
					return null;
                }

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                File file = new File(getContext()
                        .getApplicationContext()
                        .getFilesDir()
                        .getAbsolutePath()
                        + "/downloadedfile.zip");

                // Output stream
                OutputStream output = new FileOutputStream(file);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) 
				{
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

                File appPath = new File(getContext()
                        .getApplicationContext()
                        .getFilesDir()
                        .getAbsolutePath()
                        + "/UploadedImages/");

                unzip(file, appPath);

            } 
			catch (Exception e) 
			{
                Log.e("Error: ", e.getMessage());
                errorUpload(e.getMessage());
            }

            return null;
        }

        //* Unarchive data from the archive that was downloaded from the Internet
        //
        public void unzip(File zipFile, File targetDirectory) throws IOException 
		{
            ZipInputStream zis = new ZipInputStream(
                    new BufferedInputStream(new FileInputStream(zipFile)));
            try 
			{
                if (!zipFile.exists()) 
				{
                    Log.e("TravellerLog :: ", getString(R.string.problem_creating_zip_folder));
                }
				
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                // Run through all the files in the archive
                while ((ze = zis.getNextEntry()) != null) 
				{
                    File file = new File(targetDirectory, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();

                    // Add folder name to the list, to further download the pictures
                    if(!foldersForScanning.contains(dir.getName())) foldersForScanning.add(dir.getName());

                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException(getString(R.string.failed_to_ensure_directory) +
                                dir.getAbsolutePath());
                    if (ze.isDirectory())
					{
						continue;
					}
                    FileOutputStream fout = new FileOutputStream(file);
                    try 
					{
                        while ((count = zis.read(buffer)) != -1)
                            fout.write(buffer, 0, count);
                    } 
					finally 
					{
                        fout.close();
                    }
                }
            } 
			finally 
			{
                zis.close();
            }
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) 
		{
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        protected void errorUpload(final String text)
        {
            getActivity().runOnUiThread(new Runnable() 
			{
                public void run() 
				{
                    Toast.makeText(getActivity().getBaseContext(), text, Toast.LENGTH_LONG).show();
                }
            });
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) 
		{
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);
            listener.onTaskCompleted();
        }
    }

    // Start downloading the archive
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.bDownload:

                // In the thread, start downloading and decompressing
                // the archive into internal storage
                new DownloadFileFromURL(new OnTaskCompleted() 
				{
                    @Override
                    // When the archive is loaded, then count all the pictures
                    // from the folder and show on the screen
                    public void onTaskCompleted() 
					{
                        getFromSdcard();
                    }
                }).execute(editTextURL.getText().toString());

                break;
        }
    }

    /**
     * Permissions WRITE_EXTERNAL_STORAGE.
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkPermission() 
	{
        if (getContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) 
		{
            Log.v(TAG, "Permission is granted");
            return true;
        } 
		else 
		{
            Log.v(TAG, "Permission is revoked");
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) 
	{
        switch (requestCode) 
		{
            case 0:
                boolean isPerpermissionForAllGranted = false;
                if (grantResults.length > 0 && permissions.length==grantResults.length) 
				{
                    for (int i = 0; i < permissions.length; i++){
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
						{
                            isPerpermissionForAllGranted=true;
                        }
						else
						{
                            isPerpermissionForAllGranted=false;
                        }
                    }

                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } 
				else 
				{
                    isPerpermissionForAllGranted=true;
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    /**
     *  End permissions WRITE_EXTERNAL_STORAGE.
     */

    @Override
    public void onAttach(Context context) 
	{
        super.onAttach(context);
    }

    @Override
    public void onDetach() 
	{
        super.onDetach();
    }
}
