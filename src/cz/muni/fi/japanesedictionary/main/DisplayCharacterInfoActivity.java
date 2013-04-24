package cz.muni.fi.japanesedictionary.main;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.engine.MainActivity;

public class DisplayCharacterInfoActivity extends SherlockFragmentActivity
		{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("DisplayCharacterInfoActivity","Setting layout");
		setContentView(R.layout.display_activity);

		if(savedInstanceState != null){
			return ;
		}
		Bundle bundle = getIntent().getExtras();

		DisplayCharacterInfo displayCharacter= new DisplayCharacterInfo();
		displayCharacter.setArguments(bundle);
		displayCharacter.setRetainInstance(true);
		
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Log.i("DisplayCharacterInfoActivity","Setting main fragment");
		ft.add(R.id.display_fragment_container, displayCharacter);
		ft.commit();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i("DisplayCharacterInfoActivity", "Inflating menu");

	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu_details, menu);
		Log.i("DisplayCharacterInfoActivity", "Setting menu ");
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Listener for menu item selected.
	 * 
	 * @item - home item selected, restarts main activity
	 * 		 - settings item selceted, launches new MypreferenceActivity
	 * 		 - other item, default behavior
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	        	Log.i("DisplayCharacterInfoActivity", "Home button pressed");
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
	            startActivity(intent);
	            return true;
	        case R.id.settings:
    			Log.i("DisplayCharacterInfoActivity", "Lauching preference Activity");
    			Intent intentSetting = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
    			startActivity(intentSetting);
    			return true;
	        case R.id.about:
    			Log.i("DisplayCharacterInfoActivity", "Lauching About Activity");
    			Intent intentAbout = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.AboutActivity.class);
    			startActivity(intentAbout);
    			return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	

}
