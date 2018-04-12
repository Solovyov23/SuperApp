package com.example.gentl.superapp.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gentl on 3/19/2018.
 * Adapter for the viewpager using FragmentPagerAdapter
 */
public class ViewPagerAdapter extends FragmentPagerAdapter 
{
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager)
	{
        super(manager);
    }

    @Override
    public Fragment getItem(int position) 
	{
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() 
	{
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) 
	{
		// Add to every list
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) 
	{
        return mFragmentTitleList.get(position);
    }
}
