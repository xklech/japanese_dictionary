package cz.muni.fi.japanesedictionary.main;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.parser.ParserService;
import cz.muni.japanesedictionary.entity.Translation;

public class ResultFragmentList extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<List<Translation>> {

	private TranslationsAdapter mAdapter;
	private String searched = null;
	private String part = null;	
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
    	//l.requestFocus();
    	System.out.println("jeduuu");
    	mCallbackTranslation.onTranslationSelected(position);
    }
    
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		Log.i("ResultFragmentList", "Saving instance");

		if (searched != null && searched.length() > 0) {
			Log.i("ResultFragmentList", "Instance saved");
			bundle.putString(MainActivity.SEARCH_TEXT, searched.toString());
		}
		bundle.putString(MainActivity.PART_OF_TEXT, part);
		Log.i("ResultFragmentList", "saving fragmen: " + part);
		super.onSaveInstanceState(bundle);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Start out with a progress indicator.
		setListShown(false);
		SharedPreferences settings = getActivity().getSharedPreferences(
				ParserService.DICTIONARY_PREFERENCES, 0);
		boolean validDictionary = settings.getBoolean("hasValidDictionary",
				false);
		if (!validDictionary) {
			setEmptyText(getString(R.string.no_dictionary_found));
		} else {
			setEmptyText(getString(R.string.nothing_found));
		}

		mAdapter = new TranslationsAdapter(getActivity());
		((MainActivity)getActivity()).setAdapter(mAdapter);
		setListAdapter(mAdapter);

		/*getListView().setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//view.requestFocus();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		
		});*/

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public Loader<List<Translation>> onCreateLoader(int arg0, Bundle arg1) {
		System.out.println("Loader created searched: "+searched +" part: "+part);
		return new ResultLoader(getActivity(), searched, part);
	}

	public void changePart(String tabId) {
		part = tabId;
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.HANDLER_BUNDLE_TAB, tabId);
		msg.setData(bundle);
		Loader<List<Translation>> loader = getLoaderManager().getLoader(0);
		((ResultLoader) loader).getHandler().sendMessage(msg);
	}
	
	public void changeSearched(String _searched) {
		searched = _searched;
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.HANDLER_BUNDLE_TRANSLATION, searched);
		msg.setData(bundle);
		Loader<List<Translation>> loader = getLoaderManager().getLoader(0);
		((ResultLoader) loader).getHandler().sendMessage(msg);
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
	
	public String getSearched(){
		return searched;
	}
	
	public TranslationsAdapter getAdapter(){
		return mAdapter;
	}
	

}
