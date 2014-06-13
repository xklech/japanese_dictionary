/**
 *     JapaneseDictionary - an JMDict browser for Android
 Copyright (C) 2013 Jaroslav Klech
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.muni.fi.japanesedictionary.fragments;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.engine.FragmentListAsyncTask;
import cz.muni.fi.japanesedictionary.engine.TranslationsAdapter;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.interfaces.OnTranslationSelectedListener;
import cz.muni.fi.japanesedictionary.interfaces.SearchListener;
import cz.muni.fi.japanesedictionary.main.MainActivity;
import cz.muni.fi.japanesedictionary.parser.ParserService;


/**
 * Fragment list for displaying search results.
 * 
 * @author Jaroslav Klech
 *
 */
public class ResultFragmentList extends ListFragment implements
		SearchListener{
	
	private static final String LOG_TAG = "ResultFragmentList";
	
	private TranslationsAdapter mAdapter;
	
	private String mNewSearch;
	
	private String mLastSearched;
	private String mLastTab;
	private boolean mDualPane = false;
	private IncomingHandler mHandler;
	
	private FragmentListAsyncTask mLoader;

    private boolean mPreferenceCommon;

	/**
	 * Handler setting first translation as selected in dualpane mode in first run.
	 * @author Jaroslav Klech
	 *
	 */
	static class IncomingHandler extends Handler {
	    private final WeakReference<ResultFragmentList> mFragment; 

	    IncomingHandler(ResultFragmentList fragment) {
	    	mFragment = new WeakReference<>(fragment);
	    }
	    
	    /**
	     *  Sets first list item checked and if fragment is attached displays item using activity
	     * 
	     *  @param msg any message
	     */
	    @Override
	    public void handleMessage(Message msg)
	    {
	    	
	    	ResultFragmentList fragment = mFragment.get();
	         if (fragment != null) {
	        	 Log.i(LOG_TAG,"First run, select first translation");
	        	 if(fragment.isVisible()){
	        		 //is visible to user and attached to activity
	        		 fragment.getListView().setItemChecked(0, true);
	        		((OnTranslationSelectedListener)fragment.getActivity()).onTranslationSelected(0);
	        	 } 
	         }
	    }
	}
	
	/**
	 * Creates new instance of ResultFragment with params put in bundle
	 *
	 * @param part part of word to be searched in
	 * @return new instance of ResultFragmentList
	 */
    public static ResultFragmentList newInstance(String part) {
    	ResultFragmentList f = new ResultFragmentList();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString(MainActivity.PART_OF_TEXT, part);
        f.setArguments(args);

        return f;
    }

    /**
     * Broadcast receiver listening to ParserService whether parsing dictionary was done
     */
	private BroadcastReceiver mReceiverDone= new BroadcastReceiver() {
		  @Override public void onReceive(Context context, Intent intent) { 
			  //		  intent can contain anydata 
			  ResultFragmentList.this.setEmptyText(getString(R.string.nothing_found));
		  } };
		  
	private OnTranslationSelectedListener mCallbackTranslation;
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.i(LOG_TAG+": "+mLastTab, "Saving instance");

		if (mLastSearched != null) {
			outState.putString(MainActivity.SEARCH_TEXT, mLastSearched);
		}
		outState.putString(MainActivity.PART_OF_TEXT, mLastTab);
		super.onSaveInstanceState(outState);
	}


	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i(LOG_TAG+": "+mLastTab, "onAttach called");
        try {
        	mCallbackTranslation = (OnTranslationSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	if(mDualPane){
            l.setItemChecked(position, true);
    	}
    	mCallbackTranslation.onTranslationSelected(position);
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		setRetainInstance(true);
		Log.i(LOG_TAG+": "+mLastTab, "onViewCreated called - setting fragment: Last: "+mLastSearched+" new: "+mNewSearch);
		if(getActivity().findViewById(R.id.detail_fragment) != null){
			//dualPane
			mDualPane = true;
			mHandler =  new IncomingHandler(this);
		}
		SharedPreferences settings = getActivity().getSharedPreferences(
				ParserService.DICTIONARY_PREFERENCES, 0);
		String dictionaryPath = settings.getString("pathToDictionary",
				null);
		if (dictionaryPath == null || !(new File(dictionaryPath)).exists() ) {
			setEmptyText(getString(R.string.no_dictionary_found));
		} else {
			setEmptyText(getString(R.string.nothing_found));
		}
        SharedPreferences commonSetting = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferenceCommon = commonSetting.getBoolean("search_only_favorite", false);
		if(mAdapter == null){
			mAdapter = new TranslationsAdapter(getActivity());
		}else{
			Log.i(LOG_TAG+": "+mLastTab,"old adapter: "+mAdapter.getCount());
            mAdapter.updateAdapter();
		}
		setListAdapter(mAdapter);
		Bundle bundle = getArguments();
		if(savedInstanceState != null){
			mLastTab = savedInstanceState.getString(MainActivity.PART_OF_TEXT);
			mDualPane = savedInstanceState.getBoolean(MainActivity.DUAL_PANE, false);
		}else if (bundle != null) {
			mLastTab = bundle.getString(MainActivity.PART_OF_TEXT);
		}else{
			mLastTab = "exact";
		}
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE );
		mNewSearch = mCallbackTranslation.getCurrentFilter();
		Log.i(LOG_TAG+": "+mLastTab,"new search: "+mNewSearch+" old search: "+mLastSearched);

		if(mNewSearch != null && mNewSearch.equals(mLastSearched) && mAdapter.getCount()>0){
			Log.i(LOG_TAG+": "+mLastTab,"restore, no search");
            mAdapter.setLastSearchedKeb(mNewSearch);
            mAdapter.setIsExact(mLastTab.equals("exact"));
			mAdapter.notifyDataSetChanged();
			listShown(true);
		}else{
			Log.i(LOG_TAG+": "+mLastTab,"new search: "+mNewSearch+" adapter: "+mAdapter.getCount());
			mLastSearched = mNewSearch;
			mAdapter.clear();
            mAdapter.setLastSearchedKeb(mNewSearch);
            mAdapter.setIsExact(mLastTab.equals("exact"));
			mLoader = new FragmentListAsyncTask(this, getActivity());
			mLoader.execute(mLastSearched,mLastTab);
		}
		super.onViewCreated(view, savedInstanceState);
	}
	


	/**
	 * Called when FragmentListAsyncTask has finished. Sets new data to adapter.
	 * If in dual pane layout and first run sets first item as displayed.
	 */
	public void onLoadFinished(	List<Translation> data) {
		Log.i(LOG_TAG+": "+mLastTab, "Loader has finished loading data");
		// Set the new data in the adapter.
		mNewSearch = null;
		if(data!= null && data.size() > 0 && mAdapter.isEmpty()){
			mAdapter.setData(data);
		}
		if(mDualPane && mAdapter.getCount() > 0 && mLastSearched == null){
			Log.i(LOG_TAG+": "+mLastTab,"dual pane, display last - send massage");
			mHandler.sendEmptyMessage(0);
		}
		listShown(true);
	}


	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				mReceiverDone, new IntentFilter("downloadingDictinaryServiceDone"));
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				mReceiverDone);
	}
	
	/**
	 * Returns TranslationAdapter from ListFragment
	 * @return TranslationsAdapter adapter from list fragment
	 */
	public TranslationsAdapter getAdapter(){
		return mAdapter;
	}

	/**
	 * Runs new search for translations according to new expression.
	 * 
	 * @param expression to be searched for
	 */
	public void search(String expression){
        final String consonants = "qwrtzpsdfghjklyxcvbmｑｗｒｔｚｐｓｄｆｇｈｊｋｌｙｘｃｖｂｍ"; //don't run pointless queries
        if (expression != null && (expression.matches(".*[" + consonants + "]") || expression.matches("[" + consonants + "nｎ]") || expression.matches(".*[" + consonants + "nｎ]{4,}.*") || expression.matches("[　]+"))) {
            return;
        }
        if(expression != null && expression.equals(mLastSearched)){
            listShown(true);
            return;
        }
		if(mDualPane){
			getListView().setItemChecked(getListView().getCheckedItemPosition(), false);
		}
        if(mLoader != null){
            mLoader.cancel(true);
        }
        mAdapter.clear();
        mAdapter.setLastSearchedKeb(expression);
        Log.i(LOG_TAG+": "+mLastTab,"Starting nw loader: "+expression);
        listShown(false);
        mLoader = new FragmentListAsyncTask(this, getActivity());
        mLoader.execute(expression,mLastTab);
		this.mLastSearched = expression;
	}



	/**
	 * Adds individual Translations from loader
	 * 
	 * @param translation to be added
	 */
	@Override
	public void onResultFound(Translation translation) {
		mAdapter.addListItem(translation);
		/*if(this.isVisible()){
			listShown(true);
		}*/
	}
	
	private void listShown(boolean shown){
		if(getView() != null){
			/*if (isResumed()) {
				setListShown(shown);
			} else {
				setListShownNoAnimation(shown);
			}*/
            ((MainActivity)getActivity()).setSupportProgressBarVisibility(!shown);
		}
	}


    public void updateList(){
        SharedPreferences commonSetting = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean newPreferenceCommon = commonSetting.getBoolean("search_only_favorite", false);
        if(newPreferenceCommon != mPreferenceCommon){
            Log.e(LOG_TAG, "new common setting search again");
            mPreferenceCommon = newPreferenceCommon;
            if(mLoader != null){
                mLoader.cancel(true);
            }

            mAdapter.clear();
            mAdapter.updateAdapter();
            listShown(false);
            mLoader = new FragmentListAsyncTask(this, getActivity());
            mLoader.execute(mLastSearched,mLastTab);
        }
        mAdapter.updateAdapter();
    }
}
