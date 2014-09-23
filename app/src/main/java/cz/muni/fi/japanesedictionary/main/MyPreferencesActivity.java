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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;

import cz.muni.fi.japanesedictionary.Const;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.parser.ParserService;

/**
 * Preference activity for JapaneseDictionary. Containst language settings and update dictionaries info.
 *
 * @author Jaroslav Klech
 */
public class MyPreferencesActivity extends PreferenceActivity {

    private static final String LOG_TAG = "MyPreferencesActivity";

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(LOG_TAG, "Creating activity");
        addPreferencesFromResource(R.xml.preferences);

        PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("update_dictionary");
        //ads OnPreferenceClickListener on preference view for updating dictionary
        if (preferenceScreen == null) {
            return;
        }
        preferenceScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected()) {
                    Log.w(LOG_TAG, "Update dictionary - no network connection");
                    Toast.makeText(getApplicationContext(), R.string.internet_connection_failed_title, Toast.LENGTH_SHORT).show();
                } else if (!MainActivity.canWriteExternalStorage()) {
                    Log.w(LOG_TAG, "Update dictionary - can't write external storage");
                    Toast.makeText(getApplicationContext(), R.string.external_storrage_failed_title, Toast.LENGTH_SHORT).show();
                } else if (MainActivity.isMyServiceRunning(getApplicationContext())) {
                    Log.w(LOG_TAG, "Update dictionary - update in progress");
                    Toast.makeText(getApplicationContext(), R.string.updating_in_progress, Toast.LENGTH_SHORT).show();
                } else {

                    Log.i(LOG_TAG, "Update dictionary - launching service");
                    Intent intent = new Intent(getApplicationContext(), ParserService.class);
                    startService(intent);
                    Toast.makeText(getApplicationContext(), R.string.updating_in_progress, Toast.LENGTH_SHORT).show();
                }
                return false;
            }

        });

        SharedPreferences settings = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
        String dictionaryPath = settings.getString(Const.PREF_JMDICT_PATH, null);
        if (dictionaryPath == null || !(new File(dictionaryPath)).exists()) {
            preferenceScreen.setSummary(R.string.preferences_no_dictionary_info);
        } else {
            Long timestamp = settings.getLong("dictionaryLastUpdate", 0);
            if (timestamp <= 0) {
                preferenceScreen.setSummary(R.string.unknown_last_update);
            } else {
                SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
                preferenceScreen.setSummary(getString(R.string.last_update) + " " + simpleDateFormat.format(timestamp));
            }

        }
    }

	/*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_details, menu);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	        	Log.i(LOG_TAG, "Home button clicked");
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            finish();
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
            case R.id.favorites_activity:
                Log.i(LOG_TAG, "Lauching Favorite activity");
                Intent intentFavorites = new Intent(this.getApplicationContext(),FavoriteActivity.class);
                intentFavorites.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentFavorites);
                return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}*/

}