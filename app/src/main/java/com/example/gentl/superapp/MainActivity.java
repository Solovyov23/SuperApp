package com.example.gentl.superapp;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.gentl.superapp.Adapters.ViewPagerAdapter;
import com.example.gentl.superapp.Tabs.GyroscopeFragment;
import com.example.gentl.superapp.Tabs.GeonamesFragment;
import com.example.gentl.superapp.Tabs.LoaderFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		// Create ViewPager to manage fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.superViewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add Fragments to adapter one by one
        adapter.addFragment(new GeonamesFragment(), "Geonames");
        adapter.addFragment(new GyroscopeFragment(), "Gyroscope");
        adapter.addFragment(new LoaderFragment(), "Loader");
        viewPager.setAdapter(adapter);

		// Layout for pages
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
}
