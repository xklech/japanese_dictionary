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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.fragments.DisplaySentenceInfo;

public class DisplaySentenceInfoActivity extends AppCompatActivity
		{
	private static final String LOG_TAG = "DisplaySentenceInfo";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG,"Setting layout");
		setContentView(R.layout.display_activity);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if(savedInstanceState != null){
			return ;
		}
		Bundle bundle = getIntent().getExtras();

        DisplaySentenceInfo displaySentence = DisplaySentenceInfo.newInstance(bundle);
        displaySentence.setRetainInstance(true);


		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Log.i(LOG_TAG,"Setting main fragment");
		ft.add(R.id.display_fragment_container, displaySentence);
		ft.commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(LOG_TAG, "Inflating menu");
        getMenuInflater().inflate(R.menu.menu_details, menu);
        Log.i(LOG_TAG, "Setting menu ");

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Listener for menu item selected.
	 *
	 * @param item - home item selected, restarts main activity
	 * 		 - settings item selceted, launches new MypreferenceActivity
	 * 		 - other item, default behavior
	 */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Log.i(LOG_TAG, "Lauching preference Activity");
                Intent intentSetting = new Intent(this.getApplicationContext(), MyPreferencesActivity.class);
                intentSetting.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentSetting);
                return true;
            case R.id.about:
                Log.i(LOG_TAG, "Lauching About Activity");
                Intent intentAbout = new Intent(this.getApplicationContext(), AboutActivity.class);
                intentAbout.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentAbout);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
