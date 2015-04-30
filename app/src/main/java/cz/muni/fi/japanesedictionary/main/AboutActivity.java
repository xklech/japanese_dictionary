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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import cz.muni.fi.japanesedictionary.R;


/**
 * AboutActivity provides information about application.
 * @author Jaroslav Klech
 *
 */
public class AboutActivity extends AppCompatActivity {

	private static final String LOG_TAG = "AboutActivity";
	
	public static final String CREATOR = "Jaroslav Klech";

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setUpUserInterface();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        menu.findItem(R.id.about).setVisible(false);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, "Lauching About Activity");
	    switch (item.getItemId()) {
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


    /**
	 * Sets application version, creator and dictionaries acknowledgment.
	 */
	private void setUpUserInterface() {
		TextView versionView = (TextView)findViewById(R.id.about_version);
		String version;
		try{
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			
		}catch(NameNotFoundException ex){
			Log.w(LOG_TAG,"Get version exception: "+ex);
			version = getString(R.string.about_unknown);
		}
		versionView.setText(version);
		TextView nameView = (TextView)findViewById(R.id.about_name);
		nameView.setText(AboutActivity.CREATOR);
	
		
		TextView acknowledgmentView = (TextView)findViewById(R.id.about_acknowledgment);
		
	    TransformFilter mentionFilter = new TransformFilter() {
	        public final String transformUrl(final Matcher match, String url) {
	            return "";
	        }
	    };
	    
		Pattern patternJMDict = Pattern.compile("JMDict");
		String jmDictLink = "http://www.csse.monash.edu.au/~jwb/jmdict.html";
	    Linkify.addLinks(acknowledgmentView, patternJMDict, jmDictLink, null, mentionFilter);
		
	    Pattern patternKanjidic2 = Pattern.compile("KANJIDIC2");
		String kanjidic2Link = "http://www.csse.monash.edu.au/~jwb/kanjidic.html";
	    Linkify.addLinks(acknowledgmentView, patternKanjidic2, kanjidic2Link, null, mentionFilter);
	    
		Pattern patternGroup = Pattern.compile("Electronic Dictionary Research and Development Group");
		String groupLink = "http://www.edrdg.org/";
	    Linkify.addLinks(acknowledgmentView, patternGroup, groupLink, null, mentionFilter);
	
		Pattern patternLicence = Pattern.compile("licence|licencí");
		String licenceLink = "http://www.edrdg.org/edrdg/licence.html";
	    Linkify.addLinks(acknowledgmentView, patternLicence, licenceLink, null, mentionFilter);
	}

	
}
