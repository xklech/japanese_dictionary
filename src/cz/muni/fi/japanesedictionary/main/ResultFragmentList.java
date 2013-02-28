package cz.muni.fi.japanesedictionary.main;

import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.actionbarsherlock.app.SherlockListFragment;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.parser.ParserService;

public class ResultFragmentList extends SherlockListFragment 
implements LoaderManager.LoaderCallbacks<List<Translation>> {
	
	TranslationsAdapter mAdapter;
	String searched = null;
	String part = null;
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		Log.i("ResultFragmentList", "Saving instance");

		if (searched != null && searched.length() > 0) {
			Log.i("ResultFragmentList", "Instance saved");
			bundle.putString(MainActivity.SEARCH_TEXT, searched
					.toString());
		}
		bundle.putString(MainActivity.PART_OF_TEXT, part);
		Log.i("ResultFragmentList", "saving fragmen: "+part);
		super.onSaveInstanceState(bundle);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		if(savedInstanceState !=null){
			searched = savedInstanceState.getString(MainActivity.SEARCH_TEXT);
			part = savedInstanceState.getString(MainActivity.PART_OF_TEXT);
			Log.e("ResultFragmentList", part);
		}else{
			Bundle bundle = getArguments();
			if(bundle != null){
				searched = bundle.getString(MainActivity.SEARCH_TEXT);
				part = bundle.getString(MainActivity.PART_OF_TEXT);
			}
		}
		super.onCreate(savedInstanceState);
	}
	
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

		if(savedInstanceState !=null){
			searched = savedInstanceState.getString(MainActivity.SEARCH_TEXT);
			part = savedInstanceState.getString(MainActivity.PART_OF_TEXT);
			Log.e("ResultFragmentList", part);
		}
      

        // We have a menu item to show in action bar.
        //setHasOptionsMenu(true);


        // Start out with a progress indicator.
        setListShown(false);
        
        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        
        SharedPreferences settings = getActivity().getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
        boolean validDictionary = settings.getBoolean("hasValidDictionary", false);
        if(!validDictionary){
        	setEmptyText(getString(R.string.no_dictionary_found));
        }else{
        	setEmptyText("nothing found");
        }
        
        mAdapter = new TranslationsAdapter(getActivity());
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
        
    }


	@Override
	public Loader<List<Translation>> onCreateLoader(int arg0, Bundle arg1) {
		return new ResultLoader(getActivity(),searched,part);
	}


	public void changePart(String tabId){
		part = tabId;
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.HANDLER_BUNDLE_TAB, tabId);
		msg.setData(bundle);
		Loader<List<Translation>> loader = getLoaderManager().getLoader(0);
		((ResultLoader) loader).getHandler().sendMessage(msg);
	}
	
	@Override
	public void onLoadFinished(Loader<List<Translation>> loader,
			List<Translation> data) {
		
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
}
