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
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.parser.ParserService;

public class ResultFragmentList extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<List<Translation>> {
	private TranslationsAdapter mAdapter;
	
	private String mLastSearched;
	private String mLastTab;
	private boolean dualPane = false;
	private IncomingHandler handler;
	
	static class IncomingHandler extends Handler {
	    private final WeakReference<MainActivity> mActivity; 

	    IncomingHandler(MainActivity activity) {
	    	mActivity = new WeakReference<MainActivity>(activity);
	    }
	    @Override
	    public void handleMessage(Message msg)
	    {
	    	
	    	MainActivity activity = mActivity.get();
	         if (activity != null) {
	        	 Log.e("ResultFragmentList","received message");
	        	 activity.onTranslationSelected(0);
	         }else Log.e("ResultFragmentList","received message - null");
	    }
	}
	
    public static ResultFragmentList newInstance(String search,String tab) {
    	ResultFragmentList f = new ResultFragmentList();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString(MainActivity.SEARCH_TEXT, search);
        args.putString(MainActivity.PART_OF_TEXT, tab);
        f.setArguments(args);

        return f;
    }

	
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
			Log.i("ResultFragmentList", "Instance saved");
			outState.putString(MainActivity.SEARCH_TEXT, mLastSearched);
		}
		outState.putBoolean(MainActivity.DUAL_PANE, dualPane);
		outState.putString(MainActivity.PART_OF_TEXT, mLastTab);
		Log.i("ResultFragmentList", "saving fragmen: " + mLastTab);
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
    	l.setItemChecked(position, true);
    	//getListView().setSelection(position);
    	mAdapter.notifyDataSetChanged();
    	mCallbackTranslation.onTranslationSelected(position);
    }
    

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		setListShown(false);
		if(getActivity().findViewById(R.id.detail_fragment) != null){
			//dualPane
			dualPane = true;
			handler =  new IncomingHandler((MainActivity)getActivity());
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
			dualPane = savedInstanceState.getBoolean(MainActivity.DUAL_PANE, false);
		}else if (bundle != null) {
			Log.e("ResultFragmentList", "Bundle: "+bundle);
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



	@Override
	public void onLoadFinished(Loader<List<Translation>> loader,
			List<Translation> data) {
		System.out.println("Loader finished");
		// Set the new data in the adapter.
		mAdapter.setData(data);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
		if(dualPane && data!= null && data.size() > 0){
			Log.e("ResultFragmentList","dual pane, display last");
			handler.sendEmptyMessage(0);
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
	
	public TranslationsAdapter getAdapter(){
		return mAdapter;
	}


	public void search(String expression,String part){
		this.mLastSearched = expression;
		this.mLastTab = part;
        getLoaderManager().restartLoader(0, null, this);
	}
	

}
