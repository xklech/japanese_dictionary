package cz.muni.fi.japanesedictionary.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.japanesedictionary.entity.Translation;

public class DisplayCharacterInfo extends SherlockFragment{
	
	private JapaneseCharacter japaneseCharacter;
	private OnLoadGetCharacterListener mCallbackCharacter;
	private LayoutInflater inflater;
	
	public interface OnLoadGetCharacterListener{
		
		public JapaneseCharacter getJapaneseCharacter();
		
	}
	
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	mCallbackCharacter = (OnLoadGetCharacterListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoadGetCharacterListener");
        }
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.display_character, null);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState != null){
			return;
		}
		
		japaneseCharacter =  mCallbackCharacter.getJapaneseCharacter();
		setHasOptionsMenu(true);
        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_details, menu);
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.character_title));
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(getActivity(), MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.settings:
    			Log.i("MainActivity", "Lauching preference Activity");
    			Intent intentSetting = new Intent(getActivity().getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
    			intentSetting.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(intentSetting);
    			return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	    
	}

	
	
}
