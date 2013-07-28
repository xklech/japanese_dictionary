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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

import java.io.File;
import java.util.List;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.engine.MainPagerAdapter;
import cz.muni.fi.japanesedictionary.engine.TranslationsAdapter;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.fragments.DisplayCharacterInfo;
import cz.muni.fi.japanesedictionary.fragments.DisplayTranslation;
import cz.muni.fi.japanesedictionary.fragments.MyFragmentAlertDialog;
import cz.muni.fi.japanesedictionary.fragments.ResultFragmentList;
import cz.muni.fi.japanesedictionary.interfaces.OnCreateTranslationListener;
import cz.muni.fi.japanesedictionary.interfaces.OnTranslationSelectedListener;
import cz.muni.fi.japanesedictionary.parser.ParserService;


/**
 * Main Activity for JapaneseDictionary. Works with all fragments.
 * @author Jaroslav Klech
 *
 */
public class MainActivity extends SherlockFragmentActivity
	implements OnCreateTranslationListener,
				OnTranslationSelectedListener,
				OnQueryTextListener, 
				TabListener
				{
	
	private static final String LOG_TAG = "MainActivity";
	
	public static final String DUAL_PANE = "cz.muni.fi.japanesedictionary.mainactivity.dualpane";
	public static final String PARSER_SERVICE = "cz.muni.fi.japanesedictionary.parser.ParserService";
	//public static final String SEARCH_PREFERENCES = "cz.muni.fi.japanesedictionary.main.search_preferences";
	public static final String SEARCH_TEXT = "cz.muni.fi.japanesedictionary.edit_text_searched";
	public static final String PART_OF_TEXT = "cz.muni.fi.japanesedictionary.edit_text_part";
	//public static final String HANDLER_BUNDLE_TRANSLATION = "cz.muni.fi.japanesedictionary.handler_bundle_translation";
	//public static final String HANDLER_BUNDLE_TAB = "cz.muni.fi.japanesedictionary.handler_bundle_tab";
	//public static final String FRAGMENT_CREATE_TRANSLATION = "cz.muni.fi.japanesedictionary.fragment_create_translation";
	//public static final String FRAGMENT_CREATE_PART = "cz.muni.fi.japanesedictionary.fragment_create_part";
	//public static final String DISPLAY_TRANSLATION_ACTIVITY_BUNDLE = "cz.muni.fi.japanesedictionary.display_translation_activity_bundle";

	

	private GlossaryReaderContract mDatabase = null;
	
	private SearchView mSearchView;
	
	private String mLastTabId;
	private String mCurFilter;

	public static String[] mTabKeys = {"exact","beginning","middle","end"};
	
	
	private ViewPager mPager;
	
	private boolean mWaitingForConnection = false;
	
	private BroadcastReceiver mInternetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
	        boolean noConnectivity = intent.getBooleanExtra(
	                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			if (!noConnectivity) {
		        SharedPreferences settings = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
				String dictionaryPath = settings.getString("pathToDictionary", null);
				boolean waitingForConnection = settings.getBoolean("waitingForConnection", false);
				if(waitingForConnection && (dictionaryPath == null || !(new File(dictionaryPath)).exists()) ) {
					displayDownloadPrompt();
				}
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_activity);
		mDatabase = new GlossaryReaderContract(getApplicationContext());

		// Start out with a progress indicator.
		Log.i(LOG_TAG,"Controling saved instance ... ");
		if(savedInstanceState != null){
			Log.i(LOG_TAG,"Saved instance ... ");
			mCurFilter = savedInstanceState.getString(MainActivity.SEARCH_TEXT);
			mLastTabId = savedInstanceState.getString(MainActivity.PART_OF_TEXT);
			if(mLastTabId == null || mLastTabId.length()==0){
				mLastTabId = "exact";
			}
		}
		Log.i(LOG_TAG,"Find ViewPager");
		mPager = (ViewPager) findViewById(R.id.pager);	
		
        /** Defining a listener for pageChange */
		Log.i(LOG_TAG,"ViewPager listener setup");
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
                ResultFragmentList fragmentList = (ResultFragmentList) getSupportFragmentManager().findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
                if(fragmentList != null){
                	fragmentList.search(mCurFilter);
                }
                super.onPageSelected(position);

                
            }
        };
        Log.i(LOG_TAG,"ViewPager listener set");
		mPager.setOnPageChangeListener(pageChangeListener);  
		Log.i(LOG_TAG,"ViewPager create adapter");
		PagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
		Log.i(LOG_TAG,"ViewPager adapter set");
		mPager.setAdapter(adapter);
		
		
		if(mLastTabId == null || mLastTabId.length()==0){
			mLastTabId = "exact";
		}
		setUpTabs(mLastTabId);	
		
        SharedPreferences settings = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
		String dictionaryPath = settings.getString("pathToDictionary", null);
		boolean waitingForConnection = settings.getBoolean("waitingForConnection", false);
		if(waitingForConnection && (dictionaryPath == null || !(new File(dictionaryPath)).exists()) ) {
			displayDownloadPrompt();
		}
		
		Log.i(LOG_TAG,"Checking saved instance");
		if(savedInstanceState != null){
			return;
		}
		

		
		Log.i(LOG_TAG,"Setting layout");
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if(findViewById(R.id.detail_fragment) != null){
			// two frames layout
			Log.i(LOG_TAG,"Setting info fragment");
			DisplayTranslation displayTranslation = new DisplayTranslation();
			ft.add(R.id.detail_fragment, displayTranslation,"displayFragment");
		}
		ft.commit();
		if (dictionaryPath == null || !(new File(dictionaryPath)).exists() ) {
			displayDownloadPrompt();
		}
		
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(LOG_TAG, "Inflating menu");
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
		Log.i(LOG_TAG, "Setting menu ");
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Place an action bar item for searching.
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.expandActionView();
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        //mSearchView.setIconifiedByDefault(false);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		Log.i(LOG_TAG, "Setting query");
        mSearchView.setQuery(mCurFilter, false);
		Log.i(LOG_TAG, "Setting query done");

		return super.onCreateOptionsMenu(menu);
	}

	
	/**
	 *  Saves current searched text and current selected tab index.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i(LOG_TAG, "Saving instance");

		if (mCurFilter != null && mCurFilter.length() > 0) {
			outState.putString(MainActivity.SEARCH_TEXT, mCurFilter);
		}
		outState.putString(MainActivity.PART_OF_TEXT, mLastTabId);
		Log.i(LOG_TAG, "Instance saved");
		super.onSaveInstanceState(outState);
	}
	
	
	@Override
	protected void onDestroy() {
		mDatabase.close();
		super.onDestroy();
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
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	        	Log.i(LOG_TAG, "Home button pressed");
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
	            startActivity(intent);
	            return true;
	        case R.id.settings:
    			Log.i(LOG_TAG, "Lauching preference Activity");
    			Intent intentSetting = new Intent(this.getApplicationContext(),MyPreferencesActivity.class);
    			intentSetting.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    			startActivity(intentSetting);
    			return true;
	        case R.id.about:
    			Log.i(LOG_TAG, "Lauching About Activity");
    			Intent intentAbout = new Intent(this.getApplicationContext(),AboutActivity.class);
    			intentAbout.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    			startActivity(intentAbout);
    			return true;
            case R.id.favorites_activity:
                Log.i(LOG_TAG, "Lauching Favorite activity");
                Intent intentFavorites = new Intent(this.getApplicationContext(),FavoriteActivity.class);
                intentFavorites.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentFavorites);
                return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	
	/**
	 * On SearchView submit button hide keyboard. Search is done on text change.
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {
		mSearchView.clearFocus();
		return true;
	}

	/**
	 * Listener for SearchView text cahnged. Calls search method on ResultFragmentList
	 * and displays new set of entries.
	 * 
	 * @param newText changed text in SearchView
	 */
	@Override
	public boolean onQueryTextChange(String newText) {
		Log.i(LOG_TAG, "onquerychanged fird");
		
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        // Don't do anything if the filter hasn't actually changed.
        // Prevents restarting the loader when restoring state.
        if (newFilter == null) {
            return true;
        }
        if (mCurFilter != null && mCurFilter.equals(newFilter)) {
            return true;
        }
        mCurFilter = newText;
        
        ResultFragmentList fragmentList = (ResultFragmentList) getSupportFragmentManager().findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
        
        fragmentList.search(mCurFilter);
        
        
        return true;

	}
	
	
	
	/**
	 * Determins whether external storage is writable
	 * 
	 * @return true if external storage is writable else false
	 */
	public static boolean canWriteExternalStorage() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}


	/**
	 * Determines whether ParserService is already running
	 * 
	 * @param context Context of environment
	 * @return true if ParserService is running else false
	 */
	public static boolean isMyServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if(runningServices == null){
            return false;
        }
        for (RunningServiceInfo service : runningServices) {
			if (PARSER_SERVICE.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Positive choice in alert box, starts parsing dictionary
	 */
	public void doPositiveClick() {
		Log.i(LOG_TAG, "AlertBox: Download dictionary - confirm");
		downloadDictionary();
	}

	/**
	 * Negativ click of alert box, storno
	 */
	public void doNegativeClick() {
		Log.i(LOG_TAG, "AlertBox: Download dictonary - storno");
	}

	
	/**
	 * Controls internet connection, whether external storage is writalbe and ParserService isn't running. 
	 * If everything is alright, launches new ParserService.
	 */
	public void downloadDictionary() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			mWaitingForConnection = true;
			this.registerReceiver(mInternetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	        SharedPreferences settings = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putBoolean("waitingForConnection", true);
	        editor.commit();
			DialogFragment newFragment = MyFragmentAlertDialog.newInstance(
					R.string.internet_connection_failed_title,
					R.string.internet_connection_failed_message, true);
			newFragment.show(getSupportFragmentManager(), "dialog");
		} else if (!canWriteExternalStorage()) {
			DialogFragment newFragment = MyFragmentAlertDialog.newInstance(
					R.string.external_storrage_failed_title,
					R.string.external_storrage_failed_message, true);
			newFragment.show(getSupportFragmentManager(), "dialog");
		} else if (!isMyServiceRunning(getApplicationContext())) {
			if(mWaitingForConnection){
				this.unregisterReceiver(mInternetReceiver);
				mWaitingForConnection = false;
			}
			Intent intent = new Intent(this, ParserService.class);
			startService(intent);
		}
	}

	/**
	 * OnClickListener to preference item in action bar.
	 * After click launches new MyPreferenceActivity
	 * 
	 * @param w passed view
	 */
	public void showPreferences(View w){
			Log.i(LOG_TAG, "Lauching preference Activity");
			Intent intent = new Intent(getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
	}


	/**
	 * Callback method from ResultFragmentList. Responds to click on list item.
	 * Launches new DetailFragment or updates old in two pane layout on tablet.
	 * 
	 * @param index index of item in ResultFragmentList
	 */
	@Override
	public void onTranslationSelected(int index) {
		Log.i(LOG_TAG,"List Item clicked");
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		if(findViewById(R.id.detail_fragment) != null){
			// two frames layout
			Log.i(LOG_TAG,"Translation selected - Setting info fragment");
			DisplayTranslation fragment = (DisplayTranslation)fragmentManager.findFragmentByTag("displayFragment");
			if(fragment == null || !fragment.isVisible()){
	        	getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				DisplayTranslation displayFragment = new DisplayTranslation();
				Bundle bundle = getTranslationCallBack(index).createBundleFromTranslation(null);
				displayFragment.setArguments(bundle);
				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.replace(R.id.detail_fragment, displayFragment,"displayFragment");
				ft.commit();
			}else {
				//is visible
				fragment.setTranslation(getTranslationCallBack(index));
			}
			return;
		}

		Bundle bundle = getTranslationCallBack(index).createBundleFromTranslation(null);
		Intent intent = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.DisplayTranslationActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);

		
	}

	
	
	/**
	 * Method working with displayed fragment adn its adapter. Returns translation which should be displayed
	 * 
	 * @param index index of item in ResultFragmentList
	 * @return Translation translation selected from fragment lsit adapter
	 */
	private Translation getTranslationCallBack(int index){
		if(index < 0){
			return null;
		}
		
        ResultFragmentList fragmentList = (ResultFragmentList) getSupportFragmentManager().findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
    	
        if(fragmentList != null){
        	TranslationsAdapter mAdapter = fragmentList.getAdapter();

			if(mAdapter != null){
				if(mAdapter.getCount() > index){
					return mAdapter.getItem(index);
				}
			}
        }
		return null;
	}
	
	/**
	 * Returns reference to instance of SQLite database
	 * 
	 * @return GlossaryReaderContract instance of database
	 */
	public GlossaryReaderContract getDatabse(){
		return mDatabase;
	}

	/**
	 * CallBack method for DisplayTranslation fragment. Launches new DisplayCharacterInfo fragment.
	 * 
	 * @param character JapaneseCharacter to be displayed
	 */
	@Override
	public void showKanjiDetail(JapaneseCharacter character) {
		Log.i(LOG_TAG,"Setting DisplayCharacterInfo fragment");
		
		// decides whether using two pane layout or replace fragment list
		final int container = R.id.detail_fragment;		
		
		Bundle bundle = character.createBundleFromJapaneseCharacter(null);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		DisplayCharacterInfo displayCharacter = new DisplayCharacterInfo();
		displayCharacter.setArguments(bundle);
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(container, displayCharacter,"displayCharacter");
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	

	/**
	 * Called if tab is changed. Calls search method on ResultFragmentList
	 * and displays new set of entries.
	 */
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

		Log.i(LOG_TAG, "Tab changed: " + mTabKeys[tab.getPosition()]);
		String key = mTabKeys[tab.getPosition()];
		if(!key.equals(mLastTabId)){
			mLastTabId = key;
			mPager.setCurrentItem(tab.getPosition());
		}

	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}
	
	
	/**
	 * Sets up tabs for main activity
	 * @param selectedPart selected part
	 */
	private void setUpTabs(String selectedPart){
		Log.i(LOG_TAG, "Setting Tabs");
		boolean[] selectedTab = {false, false, false, false};
		if("exact".equals(selectedPart)){
			selectedTab[0] = true;
		}else if("beginning".equals(selectedPart)){
			selectedTab[1] = true;
		}else if("middle".equals(selectedPart)){
			selectedTab[2] = true;
		}else{
			selectedTab[3] = true;
		}
		
		
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		Tab tabExact = getSupportActionBar().newTab();
		tabExact.setText(R.string.search_exact);
		tabExact.setTabListener(this);
		getSupportActionBar().addTab(tabExact,selectedTab[0]);
		
		Tab tabBegin = getSupportActionBar().newTab();
		tabBegin.setText(R.string.search_beginning);
		tabBegin.setTabListener(this);
		getSupportActionBar().addTab(tabBegin,selectedTab[1]);
		
		Tab tabMiddle = getSupportActionBar().newTab();
		tabMiddle.setText(R.string.search_middle);
		tabMiddle.setTabListener(this);
		getSupportActionBar().addTab(tabMiddle,selectedTab[2]);
		
		Tab tabEnd = getSupportActionBar().newTab();
		tabEnd.setText(R.string.search_end);
		tabEnd.setTabListener(this);
		getSupportActionBar().addTab(tabEnd,selectedTab[3]);
	}
	
	
	public String getCurrentFilter(){
		return mCurFilter;
	}
	
	private String getFragmentTag(int pos){
	    return "android:switcher:"+R.id.pager+":"+pos;
	}
	
	private void displayDownloadPrompt(){
		if(!MainActivity.isMyServiceRunning(getApplicationContext())){
	        SharedPreferences settings = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putBoolean("waitingForConnection", false);
	        editor.commit();
			DialogFragment newFragment = MyFragmentAlertDialog.newInstance(
			R.string.no_dictionary_found,
			R.string.download_dictionary_question, false);
			newFragment.setCancelable(false);
			newFragment.show(getSupportFragmentManager(),
			"dialog");	
		}
	}


}
