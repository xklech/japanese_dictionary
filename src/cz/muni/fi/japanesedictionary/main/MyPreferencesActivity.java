package cz.muni.fi.japanesedictionary.main;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.parser.ParserService;

public class MyPreferencesActivity extends SherlockPreferenceActivity {


	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
			Log.e("Preferences", "Activity");
			addPreferencesFromResource(R.xml.preferences);
			
			
			PreferenceScreen preferenceScreen = (PreferenceScreen)findPreference("update_dictionary");
			preferenceScreen.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
					if (networkInfo == null || !networkInfo.isConnected()) {
						Toast.makeText(getApplicationContext(), R.string.internet_connection_failed_title, Toast.LENGTH_SHORT).show();
					} else if (!MainActivity.canWriteExternalStorage()) {
						Toast.makeText(getApplicationContext(), R.string.external_storrage_failed_title, Toast.LENGTH_SHORT).show();
					} else if (MainActivity.isMyServiceRunning(getApplicationContext())) {
						Toast.makeText(getApplicationContext(), R.string.updating_in_progress, Toast.LENGTH_SHORT).show();
					} else {					
						
						Intent intent = new Intent(getApplicationContext(), ParserService.class);
						startService(intent);
						Toast.makeText(getApplicationContext(), R.string.updating_in_progress, Toast.LENGTH_SHORT).show();

					}
					return false;
				}
				
			});
			
			SharedPreferences pref = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES,0);
			boolean has_dictionary = pref.getBoolean("hasValidDictionary", false);
			if(!has_dictionary){
				preferenceScreen.setSummary(R.string.preferences_dictionary_info);
			}else{
				Long timestamp = pref.getLong("dictionaryLastUpdate", 0);
				if(timestamp<=0){
					preferenceScreen.setSummary(R.string.unknown_last_update);
				}else{
					SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
					preferenceScreen.setSummary(getString(R.string.last_update) + " " + simpleDateFormat.format(timestamp));
				}
				
			}
			
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportActionBar().setHomeButtonEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

}