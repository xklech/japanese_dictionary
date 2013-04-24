package cz.muni.fi.japanesedictionary.main;

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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.engine.MainActivity;
import cz.muni.fi.japanesedictionary.engine.ResultLoader;
import cz.muni.fi.japanesedictionary.engine.TranslationsAdapter;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.parser.ParserService;


/**
 * Fragment list for displaying search results.
 * 
 * @author Jaroslav Klech
 *
 */
public class ResultFragmentList extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<List<Translation>> {
	private TranslationsAdapter mAdapter;
	
	private String mLastSearched;
	private String mLastTab;
	private boolean mDualPane = false;
	private IncomingHandler mHandler;
	
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
	        	 Log.i("ResultFragmentList","First run, select first translation");
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
    public static ResultFragmentList newInstance(String search,String part) {
    	ResultFragmentList f = new ResultFragmentList();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString(MainActivity.SEARCH_TEXT, search);
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
		  
		  
	public interface OnTranslationSelectedListener{
		public void onTranslationSelected(int index);
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.i("ResultFragmentList", "Saving instance");

		if (mLastSearched != null) {
			outState.putString(MainActivity.SEARCH_TEXT, mLastSearched);
		}
		outState.putBoolean(MainActivity.DUAL_PANE, mDualPane);
		outState.putString(MainActivity.PART_OF_TEXT, mLastTab);
		super.onSaveInstanceState(outState);
	}


	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.w("ResultFragmentList", "onAttach called");
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
		setListShown(false);
		Log.i("ResultFragmentList", "onViewCreated called - setting fragment");
		if(getActivity().findViewById(R.id.detail_fragment) != null){
			//dualPane
			mDualPane = true;
			mHandler =  new IncomingHandler(this);
		}
		SharedPreferences settings = getActivity().getSharedPreferences(
				ParserService.DICTIONARY_PREFERENCES, 0);
		boolean validDictionary = settings.getBoolean("hasValidDictionary",
				false);
		if (!validDictionary) {
			setEmptyText(getString(R.string.no_dictionary_found));
			if(!MainActivity.isMyServiceRunning(getActivity().getApplicationContext())){
				DialogFragment newFragment = MyFragmentAlertDialog.newInstance(
				R.string.no_dictionary_found,
				R.string.download_dictionary_question, false);
				newFragment.show(getActivity().getSupportFragmentManager(),
				"dialog");	
			}
		} else {
			setEmptyText(getString(R.string.nothing_found));
		}
		
		
		mAdapter = new TranslationsAdapter(getActivity());
		((MainActivity)getActivity()).setAdapter(mAdapter);
		setListAdapter(mAdapter);
		Bundle bundle = getArguments();
		if(savedInstanceState != null){
			mLastSearched = savedInstanceState.getString(MainActivity.SEARCH_TEXT);
			mLastTab = savedInstanceState.getString(MainActivity.PART_OF_TEXT);
			mDualPane = savedInstanceState.getBoolean(MainActivity.DUAL_PANE, false);
		}else if (bundle != null) {
			mLastSearched = bundle.getString(MainActivity.SEARCH_TEXT);
			mLastTab = bundle.getString(MainActivity.PART_OF_TEXT);
		}
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE );
		if(savedInstanceState != null){
			getLoaderManager().restartLoader(0, null, this);
		}else{
			getLoaderManager().initLoader(0, null, this);
		}
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public Loader<List<Translation>> onCreateLoader(int arg0, Bundle arg1) {
		return new ResultLoader(getActivity(), mLastSearched, mLastTab);
	}


	/**
	 * Called when ResultLoader has finished. Sets new data to adapter.
	 * If in dual pane layout and first run sets first item as displayed.
	 */
	@Override
	public void onLoadFinished(Loader<List<Translation>> loader,
			List<Translation> data) {
		Log.i("ResultFragmentList", "Loader has finished loading data");
		// Set the new data in the adapter.
		mAdapter.setData(data);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
		if(mDualPane && data!= null && data.size() > 0 && mLastSearched == null){
			Log.i("ResultFragmentList","dual pane, display last - send massage");
			mHandler.sendEmptyMessage(0);
		}

	}
	
	@Override
	public void onLoaderReset(Loader<List<Translation>> arg0) {
		mAdapter.setData(null);
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
	public void search(String expression,String part){
		this.mLastSearched = expression;
		this.mLastTab = part;
		if(mDualPane){
			getListView().setItemChecked(getListView().getCheckedItemPosition(), false);
		}
		if (isResumed()) {
			setListShown(false);
		} else {
			setListShownNoAnimation(false);
		}
        getLoaderManager().restartLoader(0, null, this);
	}
	

}
