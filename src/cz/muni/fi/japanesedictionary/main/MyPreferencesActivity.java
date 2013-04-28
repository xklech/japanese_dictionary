package cz.muni.fi.japanesedictionary.main;

import java.io.File;
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

/**
 * Preference activity for JapaneseDictionary. Containst language settings and update dictionaries info.
 * 
 * @author Jaroslav Klech
 *
 */
public class MyPreferencesActivity extends SherlockPreferenceActivity {



	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
			Log.e("Preferences", "Creating activity");
			addPreferencesFromResource(R.xml.preferences);
			
			
			PreferenceScreen preferenceScreen = (PreferenceScreen)findPreference("update_dictionary");
			//ads OnPreferenceClickListener on preference view for updating dictionary
			preferenceScreen.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
					if (networkInfo == null || !networkInfo.isConnected()) {
						Log.w("MyPreferencesActivity","Update dictionary - no network connection");
						Toast.makeText(getApplicationContext(), R.string.internet_connection_failed_title, Toast.LENGTH_SHORT).show();
					} else if (!MainActivity.canWriteExternalStorage()) {
						Log.w("MyPreferencesActivity","Update dictionary - can't write external storage");
						Toast.makeText(getApplicationContext(), R.string.external_storrage_failed_title, Toast.LENGTH_SHORT).show();
					} else if (MainActivity.isMyServiceRunning(getApplicationContext())) {
						Log.w("MyPreferencesActivity","Update dictionary - update in progress");
						Toast.makeText(getApplicationContext(), R.string.updating_in_progress, Toast.LENGTH_SHORT).show();
					} else {					
						Log.i("MyPreferencesActivity","Update dictionary - launching service");
						Intent intent = new Intent(getApplicationContext(), ParserService.class);
						startService(intent);
						Toast.makeText(getApplicationContext(), R.string.updating_in_progress, Toast.LENGTH_SHORT).show();
					}
					return false;
				}
				
			});
			
	        SharedPreferences settings = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
			String dictionaryPath = settings.getString("pathToDictionary", null);
			if(dictionaryPath == null || !(new File(dictionaryPath)).exists()){
				preferenceScreen.setSummary(R.string.preferences_dictionary_info);
			}else{
				Long timestamp = settings.getLong("dictionaryLastUpdate", 0);
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
	        	Log.i("MyPreferencesActivity", "Home button clicked");
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            finish();
	            return true;
	        case R.id.settings:
    			Log.i("MainActivity", "Lauching preference Activity");
    			Intent intentSetting = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
    			intentSetting.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    			startActivity(intentSetting);
    			return true;
	        case R.id.about:
    			Log.i("MainActivity", "Lauching About Activity");
    			Intent intentAbout = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.AboutActivity.class);
    			intentAbout.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    			startActivity(intentAbout);
    			return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

}