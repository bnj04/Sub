package com.alpinfra.subtunoid;

import java.util.Vector;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class FragmentAdapter extends FragmentPagerAdapter
{
	FragmentManager mManager;
	Vector<Fragment> localFragmentArray;

	public FragmentAdapter(FragmentManager fm, Vector<Fragment> loadFragment) 
	{
		super(fm);
		localFragmentArray = loadFragment;
		mManager = fm;
	}

	@Override
	public Fragment getItem(int arg0) {
		return localFragmentArray.get(arg0);
	}

	@Override
	public int getCount() {
		return localFragmentArray.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) 
	{
		this.notifyDataSetChanged();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) 
	{
		return super.instantiateItem(container, position);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) 
	{
		return super.isViewFromObject(arg0, arg1);
	}

	@Override
	public Parcelable saveState() 
	{
		return super.saveState();
	}
}
