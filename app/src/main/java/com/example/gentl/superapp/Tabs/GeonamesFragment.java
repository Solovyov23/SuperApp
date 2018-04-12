package com.example.gentl.superapp.Tabs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gentl.superapp.Adapters.CitiesListViewAdapter;
import com.example.gentl.superapp.DataClasses.City;
import com.example.gentl.superapp.DataClasses.GeonamesDB;
import com.example.gentl.superapp.R;

import org.geonames.FeatureClass;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeonamesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeonamesFragment extends Fragment implements TextWatcher 
{
    // Geonames user name to access API
    private final String WEBSERVICE_USERNAME = "sunflower23";

    // Views
    ListView lvGeonamesData;
    private AutoCompleteTextView autoCompleteTextView;

    // Adapters and related data
    private CitiesListViewAdapter citiesListViewAdapter;
    private ArrayAdapter<String> adapterAutoComplete;
    private ArrayList<String> сitiesArrayList = new ArrayList<String> ();

    // Only one thread fills autocomplete dropdown
    AutocompleteThread AutoCompleteThread;


    public GeonamesFragment() 
	{
        // Required empty public constructor
    }

    public static GeonamesFragment newInstance()
	{
        GeonamesFragment fragment = new GeonamesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) 
	{
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_geonames, container, false);

        // setup simple adapter for autoCompleteTextView
        adapterAutoComplete = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, сitiesArrayList);
        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(adapterAutoComplete);
        autoCompleteTextView.addTextChangedListener(this);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() 
		{
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
				// Get the selected world and add it to the database
                final City city = new City(-1, autoCompleteTextView.getText().toString());
                citiesListViewAdapter.addCity(city);
                Toast.makeText( getActivity().getBaseContext(), 
								getString(R.string.added) + city.getTitleCity(),
								Toast.LENGTH_SHORT).show();
            }
        });

        //LIST VIEW
        // Open the connection to the database and get the cursor
        Cursor cursor = GeonamesDB.getInstance(getContext()).getAllCities();

        // Create the adapter and configure the list
        citiesListViewAdapter = new CitiesListViewAdapter(getActivity(), cursor);
        lvGeonamesData = view.findViewById(R.id.lvGeonamesData);
        lvGeonamesData.setAdapter(citiesListViewAdapter);

        // Long press on the list of saved cities
        lvGeonamesData.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() 
		{
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                // Open the AlertDialog for a long time and confirm the deletion
                final City city = citiesListViewAdapter.getCityFromPosition(position);
                AlertDialog.Builder adb=new AlertDialog.Builder(getContext());
                adb.setTitle(R.string.delete);
                adb.setMessage(getString(R.string.are_you_sure_you_want_to_delete) + city.getTitleCity());
                adb.setNegativeButton(R.string.сancel, null);
                adb.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() 
				{
                    public void onClick(DialogInterface dialog, int which) 
					{
                        // Remove City from DB
                        if(citiesListViewAdapter.removeCity(city.getIdCity()))
                        {
                            Toast.makeText(getActivity().getBaseContext(), getString(R.string.deleted) + city.getTitleCity(), Toast.LENGTH_SHORT).show();
                        }
                    }});
                adb.show();

                return true;
            }
        });
        return view;
    }

    // * When a user enters a city name by character
    // *
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {
        // If the user entered only two characters, but not search
        if(charSequence.length() <= 2)
        {
            autoCompleteTextViewDismissDropDown();
            return;
        }
        // Check if the thread is running, if so, stop it
        if(AutoCompleteThread != null && AutoCompleteThread.isAlive())
        {
            AutoCompleteThread.stopThread();
            autoCompleteTextViewDismissDropDown();
        }

        // Start searching cities in a separate thread
        AutoCompleteThread = new AutocompleteThread(charSequence.toString());
        AutoCompleteThread.start();
    }

    // * Collapsing drop-down matches if there is no reason to look at them
    // *
    public void autoCompleteTextViewDismissDropDown()
    {
        if(autoCompleteTextView.isPopupShowing() == true) 
		{
            autoCompleteTextView.dismissDropDown();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
	{

    }

    @Override
    public void afterTextChanged(Editable editable) 
	{

    }

    // *
    // Send search request and find possible, similar cities
    // *
    public class AutocompleteThread extends Thread 
	{
        private Boolean relevantSearch = true;
        // stores the name of the city for search
        private String titleCity = "";
        public AutocompleteThread(String titleCity) 
		{
            this.titleCity = titleCity;
        }

        // Stop the flow if it is not up-to-date
        // (the search word has been changed)
        public void stopThread()
        {
            relevantSearch = false;
        }

        public void run() 
		{
            ToponymSearchResult searchResult = null;
            try
            {
                // To delay the thread, the user entered multiple characters at once
                Thread.sleep(600);

                // Stop the flow if it is not up-to-date
                // (the search word has been changed)
                if(!relevantSearch)
                {
                    return;
                }

                // The user's registration for accessing the site geonames.org
                WebService.setUserName(WEBSERVICE_USERNAME);

                // Find the city
                ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();

                // Use the FeatureCode of P (Populated Area)
                searchCriteria.setFeatureClass(FeatureClass.P);
                // сitiesArrayList and so on
                searchCriteria.setFeatureCode("PPL");
                searchCriteria.setNameStartsWith(titleCity/*.toLowerCase()*/);
                searchCriteria.setStyle(Style.FULL);
                // The maximum amount of data requested by the data
                searchCriteria.setMaxRows(10);

                // Send request with parameters
                searchResult = WebService.search(searchCriteria);

                // Stop the flow if it is not up-to-date
                // (the search word has been changed)
                if(!relevantSearch)
                {
                    return;
                }

                // Clear the old list of cities and fill it with a new result
                сitiesArrayList.clear();
                for (Toponym toponym : searchResult.getToponyms())
                {
                    // Some names can repeat. Handle this...
                    if(!сitiesArrayList.contains(toponym.getName())) 
					{
                        сitiesArrayList.add(toponym.getName());
                    }
                }

                // Contact the main thread and update the data in the list of saved cities
                getActivity().runOnUiThread(new Runnable() 
				{
                    public void run() 
					{
						autoCompleteTextView.setAdapter
						(
							new ArrayAdapter<String>(getContext(),
							android.R.layout.simple_dropdown_item_1line,
							(ArrayList<String>) сitiesArrayList.clone())
						);
						if(autoCompleteTextView.isPopupShowing() == false && relevantSearch == true) 
						{
							autoCompleteTextView.showDropDown();
						}
                    }
                });

            }
            catch (final Exception e)
            {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() 
				{
                    public void run() 
					{
                        Toast.makeText(getActivity().getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    public void onResume() 
	{
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        // Stop the search process and stream

        autoCompleteTextViewDismissDropDown();
        if(AutoCompleteThread != null && AutoCompleteThread.isAlive())
        {
            AutoCompleteThread.stopThread();
            autoCompleteTextViewDismissDropDown();
        }
        AutoCompleteThread = null;
    }

    @Override
    public void onAttach(Context context) 
	{
        super.onAttach(context);
    }

    @Override
    public void onDestroy() 
	{
        super.onDestroy();
        GeonamesDB.getInstance(getContext()).close();
    }

    @Override
    public void onDetach() 
	{
        super.onDetach();
    }
}
