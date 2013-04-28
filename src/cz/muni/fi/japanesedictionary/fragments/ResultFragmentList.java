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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

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
public class ResultFragmentList extends SherlockListFragment implements
		SearchListener{
	
	public static final String TAG = "ResultFragmentList";
	
	private TranslationsAdapter mAdapter;
	
	private String mNewSearch;
	
	private String mLastSearched;
	private String mLastTab;
	private boolean mDualPane = false;
	private IncomingHandler mHandler;
	
	private FragmentListAsyncTask mLoader;

	
	
	/**
	 * Handler setting first translation as selected in dualpane mode in first run.
	 * @author Jaroslav Klech
	 *
	 */
	static class IncomingHandler extends Handler {
	    private final WeakReference<ResultFragmentList> mFragment; 

	    IncomingHandler(ResultFragmentList fragment) {
	    	mFragment = new WeakReference<ResultFragmentList>(fragment);
	    }
	    
	    /**
	     *  Sets first list item checked and if fragment is attached displays item using activity
	     * 
	     *  @param any message
	     */
	    @Override
	    public void handleMessage(Message msg)
	    {
	    	
	    	ResultFragmentList fragment = mFragment.get();
	         if (fragment != null) {
	        	 Log.i(TAG,"First run, select first translation");
	        	 if(fragment.isVisible()){
	        		 //is visible to user and attached to activity
	        		 fragment.getListView().setItemChecked(0, true);
	        		((MainActivity)fragment.getActivity()).onTranslationSelected(0);
	        	 } 
	         }
	    }
	}
	
	/**
	 * Creates new instance of ResultFragment with params put in bundle
	 * 
	 * @param search string to be searched for
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
		Log.i(TAG+": "+mLastTab, "Saving instance");

		if (mLastSearched != null) {
			outState.putString(MainActivity.SEARCH_TEXT, mLastSearched);
		}
		outState.putString(MainActivity.PART_OF_TEXT, mLastTab);
		super.onSaveInstanceState(outState);
	}


	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.w(TAG+": "+mLastTab, "onAttach called");
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
		Log.i(TAG+": "+mLastTab, "onViewCreated called - setting fragment: Last: "+mLastSearched+" new: "+mNewSearch);
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
		
		if(mAdapter == null){
			mAdapter = new TranslationsAdapter(getActivity());
		}else{
			Log.e(TAG+": "+mLastTab,"old adapter: "+mAdapter.getCount());
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
		Log.e(TAG+": "+mLastTab,"new search: "+mNewSearch+" old search: "+mLastSearched);
		
		if(mNewSearch != null && mNewSearch.equals(mLastSearched) && mAdapter.getCount()>0){
			Log.i(TAG+": "+mLastTab,"restore, no search");
			mAdapter.notifyDataSetChanged();
			listShown(true);
		}else{
			Log.i(TAG+": "+mLastTab,"new search: "+mNewSearch+" adapter: "+mAdapter.getCount());
			mLastSearched = mNewSearch;
			mAdapter.clear();
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
		Log.i(TAG+": "+mLastTab, "Loader has finished loading data");
		// Set the new data in the adapter.
		mNewSearch = null;
		if(data!= null && data.size() > 0 && mAdapter.isEmpty()){
			System.out.println("zobrazit vse");
			mAdapter.setData(data);
		}
		if(mDualPane && mAdapter.getCount() > 0 && mLastSearched == null){
			Log.i(TAG+": "+mLastTab,"dual pane, display last - send massage");
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
	 * Runs new search for translations according to new expression and word part.
	 * 
	 * @param expression to be searched for
	 * @param part to be searched in
	 */
	public void search(String expression){
		if(expression == null && this.mLastSearched == null){
			return ;
		}

		if(mDualPane){
			getListView().setItemChecked(getListView().getCheckedItemPosition(), false);
		}
		if(expression!= null && !expression.equals(this.mLastSearched)){
			if(mLoader != null){
				mLoader.cancel(true);
			}
			mAdapter.clear();
			Log.i(TAG+": "+mLastTab,"Starting nw loader: "+expression);
			mLoader = new FragmentListAsyncTask(this, getActivity());
			mLoader.execute(expression,mLastTab);
		}else{
			if(this.isVisible()){
				listShown(true);
			}
		}
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
		if(this.isVisible()){
			listShown(true);
		}
	}
	
	private void listShown(boolean shown){
		if(getView() != null){
			if (isResumed()) {
				setListShown(shown);
			} else {
				setListShownNoAnimation(shown);
			}
		}
	}

}
