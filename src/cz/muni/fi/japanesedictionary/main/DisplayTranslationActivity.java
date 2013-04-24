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
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;

public class DisplayTranslationActivity extends SherlockFragmentActivity
		implements DisplayTranslation.OnCreateTranslationListener{

	private GlossaryReaderContract mDatabase = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("DisplayTranslationActivity","Setting layout");
		setContentView(R.layout.display_activity);
		mDatabase = new GlossaryReaderContract(getApplicationContext());
		if(savedInstanceState != null){
			return ;
		}
		Bundle bundle = getIntent().getExtras();

		DisplayTranslation displayTranslation= new DisplayTranslation();
		displayTranslation.setArguments(bundle);
		displayTranslation.setRetainInstance(true);
		
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Log.i("DisplayTranslationActivity","Setting main fragment");
		ft.add(R.id.display_fragment_container, displayTranslation);
		ft.commit();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i("DisplayTranslationActivity", "Inflating menu");

	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu_details, menu);
		Log.i("DisplayTranslationActivity", "Setting menu ");
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
	        	Log.i("DisplayTranslationActivity", "Home button pressed");
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
	            startActivity(intent);
	            return true;
	        case R.id.settings:
    			Log.i("DisplayTranslationActivity", "Lauching preference Activity");
    			Intent intentSetting = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
    			startActivity(intentSetting);
    			return true;
	        case R.id.about:
    			Log.i("DisplayTranslationActivity", "Lauching About Activity");
    			Intent intentAbout = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.AboutActivity.class);
    			startActivity(intentAbout);
    			return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onDestroy() {
		mDatabase.close();
		super.onDestroy();
	}
	
	
	
	
	/**
	 * CallBack method for DisplayTranslation fragment. Launches new DisplayCharacterInfo fragment.
	 * 
	 * @param character JapaneseCharacter to be displayed
	 */
	@Override
	public void showKanjiDetail(JapaneseCharacter character) {
		Log.i("DisplayTranslationActivity","Setting DisplayCharacterInfo Activity");
		Bundle bundle = character.createBundleFromJapaneseCharacter(null);

		Intent intent = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.DisplayCharacterInfoActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
				
	}
	
	
	
	/**
	 * Returns reference to instance of SQLite database
	 * 
	 * @return GlossaryReaderContract instance of database
	 */
	@Override
	public GlossaryReaderContract getDatabse(){
		return mDatabase;
	}
	
}
