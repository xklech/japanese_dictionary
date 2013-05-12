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
import cz.muni.fi.japanesedictionary.fragments.DisplayCharacterInfo;

public class DisplayCharacterInfoActivity extends SherlockFragmentActivity
		{
	private static final String LOG_TAG = "DisplayCharacterInfoActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG,"Setting layout");
		setContentView(R.layout.display_activity);

		if(savedInstanceState != null){
			return ;
		}
		Bundle bundle = getIntent().getExtras();

		DisplayCharacterInfo displayCharacter= new DisplayCharacterInfo();
		displayCharacter.setArguments(bundle);
		displayCharacter.setRetainInstance(true);
		
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Log.i(LOG_TAG,"Setting main fragment");
		ft.add(R.id.display_fragment_container, displayCharacter);
		ft.commit();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(LOG_TAG, "Inflating menu");

	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu_details, menu);
		Log.i(LOG_TAG, "Setting menu ");
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
	        	Log.i(LOG_TAG, "Home button pressed");
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
	            startActivity(intent);
	            return true;
	        case R.id.settings:
    			Log.i(LOG_TAG, "Lauching preference Activity");
    			Intent intentSetting = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
    			intentSetting.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    			startActivity(intentSetting);
    			return true;
	        case R.id.about:
    			Log.i(LOG_TAG, "Lauching About Activity");
    			Intent intentAbout = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.AboutActivity.class);
    			intentAbout.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    			startActivity(intentAbout);
    			return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	

}
