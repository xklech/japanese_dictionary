package cz.muni.fi.japanesedictionary.engine;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import cz.muni.fi.japanesedictionary.fragments.ResultFragmentList;
import cz.muni.fi.japanesedictionary.main.MainActivity;

public class MainPagerAdapter extends FragmentPagerAdapter{
	
	
	public MainPagerAdapter(FragmentManager fm) {
		super(fm);

	}

	@Override
	public Fragment getItem(int index) {
		ResultFragmentList list = ResultFragmentList.newInstance(MainActivity.mTabKeys[index]);
		return list;
	}

	@Override
	public int getCount() {
		
		return 4;
	}

	
	
	
	
}
