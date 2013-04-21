package cz.muni.fi.japanesedictionary.main;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnCloseListener;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.parser.ParserService;


/**
 * Main Activity for JapaneseDictionary. Works with all fragments.
 * @author Jaroslav Klech
 *
 */
public class MainActivity extends SherlockFragmentActivity
	implements ResultFragmentList.OnTranslationSelectedListener,
				DisplayTranslation.OnCreateTranslationListener,
				DisplayCharacterInfo.OnLoadGetCharacterListener,
				OnQueryTextListener, 
				OnCloseListener,
				TabHost.OnTabChangeListener
				{
	
	public static final String DUAL_PANE = "cz.muni.fi.japanesedictionary.mainactivity.dualpane";
	public static final String PARSER_SERVICE = "cz.muni.fi.japanesedictionary.parser.ParserService";
	public static final String SEARCH_PREFERENCES = "cz.muni.fi.japanesedictionary.main.search_preferences";
	public static final String SEARCH_TEXT = "cz.muni.fi.japanesedictionary.edit_text_searched";
	public static final String PART_OF_TEXT = "cz.muni.fi.japanesedictionary.edit_text_part";
	public static final String HANDLER_BUNDLE_TRANSLATION = "cz.muni.fi.japanesedictionary.handler_bundle_translation";
	public static final String HANDLER_BUNDLE_TAB = "cz.muni.fi.japanesedictionary.handler_bundle_tab";
	public static final String FRAGMENT_CREATE_TRANSLATION = "cz.muni.fi.japanesedictionary.fragment_create_translation";
	public static final String FRAGMENT_CREATE_PART = "cz.muni.fi.japanesedictionary.fragment_create_part";
	
	
	private TranslationsAdapter mAdapter = null;
	private GlossaryReaderContract mDatabase = null;
	private JapaneseCharacter mJapaneseCharacter;
	
	private ResultFragmentList mFragmentList = null;
	
	private SearchView mSearchView;
	
	private TabHost mTabHost;
	private String mLastTabId;
	private String mCurFilter;
	
	
	/**
	 * Sets TranslationAdapter from ListFragment 
	 * 
	 * @param adapter adapter to be set
	 */
	public void setAdapter(TranslationsAdapter adapter){
		mAdapter = adapter;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		mDatabase = new GlossaryReaderContract(getApplicationContext());

		// Start out with a progress indicator.
		Log.i("MainFragment", "Setting Tabs");
		mTabHost = (TabHost) this.findViewById(android.R.id.tabhost);
	

		mTabHost.setup();
		
		mTabHost.addTab(mTabHost.newTabSpec("exact").setIndicator(getText(R.string.search_exact))
				.setContent(new TabFactory(this)));
		mTabHost.addTab(mTabHost.newTabSpec("begining").setIndicator(getText(R.string.search_begining))
				.setContent(new TabFactory(this)));
		mTabHost.addTab(mTabHost.newTabSpec("middle").setIndicator(getText(R.string.search_middle))
				.setContent(new TabFactory(this)));
		mTabHost.addTab(mTabHost.newTabSpec("end").setIndicator(getText(R.string.search_end))
				.setContent(new TabFactory(this)));
		mTabHost.setOnTabChangedListener(this);
		Log.i("MainActivity","Checking saved instance");
		if(savedInstanceState != null){
			mCurFilter = savedInstanceState.getString(MainActivity.SEARCH_TEXT);
			mLastTabId = savedInstanceState.getString(MainActivity.PART_OF_TEXT);
			mFragmentList = (ResultFragmentList) getSupportFragmentManager().findFragmentByTag("resultFragmentList");
			if(mLastTabId == null || mLastTabId.length()==0){
				mLastTabId = "exact";
			}else{
				mTabHost.setCurrentTabByTag(mLastTabId);
			}
			return;
		}
		if(mLastTabId == null || mLastTabId.length()==0){
			mLastTabId = "exact";
		}else{
			mTabHost.setCurrentTabByTag(mLastTabId);
		}

		Log.i("MainActivity","Setting layout");
		mFragmentList = new ResultFragmentList();
		mFragmentList.setRetainInstance(true);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Log.i("MainActivity","Setting main fragment");
		ft.add(android.R.id.tabcontent, mFragmentList,"resultFragmentList");
		if(findViewById(R.id.detail_fragment) != null){
			// two frames layout
			Log.i("MainActivity","Setting info fragment");
			DisplayTranslation displayTranslation = new DisplayTranslation();
			ft.add(R.id.detail_fragment, displayTranslation,"displayFragment");
		}
		ft.commit();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i("MainFragment", "Inflating menu");

	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
		Log.i("MainFragment", "Setting menu ");
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // Place an action bar item for searching.
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQuery(mCurFilter, true);
		return super.onCreateOptionsMenu(menu);
	}

	
	/**
	 *  Saves current searched text and current selected tab index.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i("MainActivity", "Saving instance");

		if (mCurFilter != null && mCurFilter.length() > 0) {
			outState.putString(MainActivity.SEARCH_TEXT, mCurFilter);
		}
		outState.putString(MainActivity.PART_OF_TEXT, mLastTabId);
		Log.i("MainActivity", "Instance saved");
		super.onSaveInstanceState(outState);
	}
	
	
	@Override
	protected void onDestroy() {
		mDatabase.close();
		super.onDestroy();
	}

	/**
	 * Overrides SearchView onClose behavior. Does not erase text.
	 * 
	 */
	@Override
	public boolean onClose() {
		
        return true;
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
	        	Log.i("MainActivity", "Home button pressed");
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
	            startActivity(intent);
	            return true;
	        case R.id.settings:
    			Log.i("MainActivity", "Lauching preference Activity");
    			Intent intentSetting = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
    			startActivity(intentSetting);
    			return true;
	        case R.id.about:
    			Log.i("MainActivity", "Lauching About Activity");
    			Intent intentAbout = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.AboutActivity.class);
    			startActivity(intentAbout);
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
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        // Don't do anything if the filter hasn't actually changed.
        // Prevents restarting the loader when restoring state.
        if (mCurFilter == null && newFilter == null) {
            return true;
        }
        if (mCurFilter != null && mCurFilter.equals(newFilter)) {
            return true;
        }
        mCurFilter = newText;
        if(!mFragmentList.isVisible()){
        	getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        	mFragmentList = ResultFragmentList.newInstance(mCurFilter,mLastTabId);
        	ft.replace(android.R.id.tabcontent, mFragmentList, "resultFragmentList");
        	Log.i("MainActivity","Text changed - launching new fragmentList");	

        	ft.commit();
        }else{
        	Log.i("MainActivity","Text changed - updating visible fragmentList");
        	mFragmentList.search(mCurFilter, mLastTabId);
        }
        
        
        return true;

	}
	
	

	/**
	 * Called if tab is changed. Calls search method on ResultFragmentList
	 * and displays new set of entries.
	 * 
	 * @param tabId id of changed tab
	 */
	@Override
	public void onTabChanged(String tabId) {
		Log.i("MainActivity", "Tab changed: " + tabId);

		if(tabId != mLastTabId){
			mLastTabId = tabId;
	        if(!mFragmentList.isVisible()){
	        	//clear backstack
	        	getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	        	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        	mFragmentList = ResultFragmentList.newInstance(mCurFilter,mLastTabId);
	        	ft.replace(android.R.id.tabcontent, mFragmentList, "resultFragmentList");
	        	Log.i("MainActivity","tab changed - launching new fragmentList");	       
	        	
	        	ft.commit();
	        }else{
	        	Log.i("MainActivity","tab changed - updating visible fragmentList");
	        	mFragmentList.search(mCurFilter, mLastTabId);
	        }
	        
		}

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
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
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
		Log.i("MainActivity", "AlertBox: Download dictionary - confirm");
		downloadDictionary();
	}

	/**
	 * Negativ click of alert box, storno
	 */
	public void doNegativeClick() {
		Log.i("MainActivity", "AlertBox: Download dictonary - storno");
	}

	
	/**
	 * Controls internet connection, whether external storage is writalbe and ParserService isn't running. 
	 * If everything is alright, launches new ParserService.
	 */
	public void downloadDictionary() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
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
			Log.i("MainActivity", "Lauching preference Activity");
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
		Log.i("MainActivity","List Item clicked");
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		if(findViewById(R.id.detail_fragment) != null){
			// two frames layout
			Log.i("MainActivity","Translation selected - Setting info fragment");
			DisplayTranslation fragment = (DisplayTranslation)fragmentManager.findFragmentByTag("displayFragment");
			if(fragment == null || !fragment.isVisible()){
				DisplayTranslation displayFragment = new DisplayTranslation();
				Bundle bundle = new Bundle();
				bundle.putInt("TranslationId", index);
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
		DisplayTranslation displayFragment = new DisplayTranslation();
		Bundle bundle = new Bundle();
		bundle.putInt("TranslationId", index);
		displayFragment.setArguments(bundle);
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(android.R.id.tabcontent, displayFragment,"displayFragment");
		ft.addToBackStack(null);
		ft.commit();
		
	}
	
	/**
	 * CallBack method for DisplayTranslation fragment. Returns translation which should be displayed
	 * 
	 * @param index index of item in ResultFragmentList
	 * @return Translation translation selected from fragment lsit adapter
	 */
	@Override
	public Translation getTranslationCallBack(int index){
		if(mAdapter == null && mFragmentList != null){
			mAdapter = mFragmentList.getAdapter();
		}
		
		if(mAdapter != null){
			if(mAdapter.getCount() > index){
				return mAdapter.getItem(index);
				
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
		mJapaneseCharacter  = character;
		Log.i("MainActivity","Setting DisplayCharacterInfo fragment");
		
		// decides whether using two pane layout or replace fragment list
		final int container = (findViewById(R.id.detail_fragment) != null) ?R.id.detail_fragment:android.R.id.tabcontent;		
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		DisplayCharacterInfo displayCharacter = new DisplayCharacterInfo();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(container, displayCharacter,"displayCharacter");
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	
	/**
	 * CallBack for DisplayCharacterInfo fragment. Returns JapaneseCharacter which
	 * should be displayed to user.
	 * 
	 * @return JapaneseCharacter
	 */
	@Override
	public JapaneseCharacter getJapaneseCharacter() {
		return mJapaneseCharacter;
	}
	
    
	/**
	 * Tab factory for creating new tabs
	 * @author Jaroslav Klech
	 */
	static class TabFactory implements TabHost.TabContentFactory {
		private final Context mContext;

		public TabFactory(Context context) {
			mContext = context;
		}

		@Override
		public View createTabContent(String tag) {			
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}
}
