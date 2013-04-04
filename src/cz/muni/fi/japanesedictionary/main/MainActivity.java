package cz.muni.fi.japanesedictionary.main;

import android.annotation.SuppressLint;
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

public class MainActivity extends SherlockFragmentActivity
	implements ResultFragmentList.OnTranslationSelectedListener,
				DisplayTranslation.OnCreateTranslationListener,
				DisplayCharacterInfo.OnLoadGetCharacterListener,
				OnQueryTextListener, 
				OnCloseListener,
				TabHost.OnTabChangeListener
				{
	
	public static final String PARSER_SERVICE = "cz.muni.fi.japanesedictionary.parser.ParserService";
	public static final String SEARCH_PREFERENCES = "cz.muni.fi.japanesedictionary.main.search_preferences";
	public static final String SEARCH_TEXT = "cz.muni.fi.japanesedictionary.edit_text_searched";
	public static final String PART_OF_TEXT = "cz.muni.fi.japanesedictionary.edit_text_part";
	public static final String HANDLER_BUNDLE_TRANSLATION = "cz.muni.fi.japanesedictionary.handler_bundle_translation";
	public static final String HANDLER_BUNDLE_TAB = "cz.muni.fi.japanesedictionary.handler_bundle_tab";
	public static final String FRAGMENT_CREATE_TRANSLATION = "cz.muni.fi.japanesedictionary.fragment_create_translation";
	public static final String FRAGMENT_CREATE_PART = "cz.muni.fi.japanesedictionary.fragment_create_part";
	
	
	private TranslationsAdapter mAdapter = null;
	private GlossaryReaderContract database = null;
	private JapaneseCharacter japaneseCharacter;
	
	private ResultFragmentList fragmentList = null;
	
	private SearchView mSearchView;
	
	private TabHost mTabHost;
	private String mLastTabId;
	private String mCurFilter;
	public void setAdapter(TranslationsAdapter _adapter){
		mAdapter = _adapter;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("MainActivity","Checking saved instance");
		setContentView(R.layout.main_fragment);
		database = new GlossaryReaderContract(getApplicationContext());
		//setHasOptionsMenu(true);
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
		if(savedInstanceState != null){
			mCurFilter = savedInstanceState.getString(MainActivity.SEARCH_TEXT);
			mLastTabId = savedInstanceState.getString(MainActivity.PART_OF_TEXT);
			fragmentList = (ResultFragmentList) getSupportFragmentManager().findFragmentByTag("resultFragmentList");
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
		fragmentList = new ResultFragmentList();
		fragmentList.setRetainInstance(true);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Log.i("MainActivity","Setting main fragment");
		ft.add(android.R.id.tabcontent, fragmentList,"resultFragmentList");
		if(findViewById(R.id.detail_fragment) != null){
			// two frames layout
			Log.i("MainActivity","Setting info fragment");
			DisplayTranslation displayTranslation = new DisplayTranslation();
			ft.add(R.id.detail_fragment, displayTranslation,"displayFragment");
			ft.addToBackStack("mainFragment");
		}
		ft.commit();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i("MainFragment", "Inflating menu");

	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
		Log.i("MainFragment", "Setting menu ");
		getSupportActionBar().setHomeButtonEnabled(true);
		
        // Place an action bar item for searching.
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconifiedByDefault(false);
        
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i("ResultFragmentList", "Saving instance");

		if (mCurFilter != null && mCurFilter.length() > 0) {
			Log.i("ResultFragmentList", "Instance saved");
			outState.putString(MainActivity.SEARCH_TEXT, mCurFilter);
		}
		outState.putString(MainActivity.PART_OF_TEXT, mLastTabId);
		Log.i("ResultFragmentList", "saving fragmen: " + mLastTabId);
		super.onSaveInstanceState(outState);
	}
	
	
	@Override
	protected void onDestroy() {
		database.close();
		super.onDestroy();
	}

	@Override
	public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            //mSearchView.setQuery("", false);
        }
        return true;

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
	            startActivity(intent);
	            return true;
	        case R.id.settings:
    			Log.i("MainActivity", "Lauching preference Activity");
    			Intent intentSetting = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
    			startActivity(intentSetting);
    			return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		mSearchView.clearFocus();
		return true;
	}


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
		//fragmentList = getSupportFragmentManager().findFragmentByTag("")
        mCurFilter = newText;
        if(!fragmentList.isVisible()){
        	getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        	fragmentList = ResultFragmentList.newInstance(mCurFilter,mLastTabId);
        	ft.replace(android.R.id.tabcontent, fragmentList, "resultFragmentList");
        	Log.i("MainActivity","text changed - isnt visible");	

        	ft.commit();
        }else{
        	Log.i("MainActivity","text changed is vidible");
        }
        fragmentList.search(mCurFilter, mLastTabId);
        
        return true;

	}
	
	


	@Override
	public void onTabChanged(String tabId) {
		Log.i("MainActivity", "Tab changed: " + tabId);

		if(tabId != mLastTabId){
			mLastTabId = tabId;
	        if(!fragmentList.isVisible()){
	        	//clear backstack
	        	getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	        	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	        	fragmentList = ResultFragmentList.newInstance(mCurFilter,mLastTabId);
	        	ft.replace(android.R.id.tabcontent, fragmentList, "resultFragmentList");
	        	Log.i("MainActivity","tab changed - isnt visible");	       
	        	
	        	ft.commit();
	        }else{
	        	Log.i("MainActivity","tab changed is vidible");
	        	fragmentList.search(mCurFilter, mLastTabId);
	        }
	        
		}

	}
	
	
	
	
	public static boolean canWriteExternalStorage() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}



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


	public void doPositiveClick() {
		Log.i("MainActivity", "AlertBox: Download dictionary - download");
		downloadDictionary();
	}

	public void doNegativeClick() {
		Log.i("MainActivity", "AlertBox: Download dictonary - storno");
	}

	

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
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startService(intent);
		}
	}


	@SuppressLint("NewApi")
	public void showPreferences(View w){
			Log.i("MainActivity", "Lauching preference Activity");
			Intent intent = new Intent(getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
	}



	@Override
	public void onTranslationSelected(int index) {
		Log.i("MainActivity","Item clicked: ");
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		if(findViewById(R.id.detail_fragment) != null){
			// two frames layout
			Log.i("MainActivity","Setting info fragment");
			DisplayTranslation fragment = (DisplayTranslation)fragmentManager.findFragmentByTag("displayFragment");
			if(fragment == null ){
				DisplayTranslation displayFragment = new DisplayTranslation();
				Bundle bundle = new Bundle();
				bundle.putInt("TranslationId", index);
				displayFragment.setArguments(bundle);
				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.replace(R.id.main_fragment, displayFragment,"displayFragment");
				ft.commit();
			}else if(fragment.isDetached()){
				fragment.setTranslation(getTranslationCallBack(index));
				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.replace(R.id.main_fragment, fragment,"displayFragment");
				ft.commit();
			}else{
				fragment.setTranslation(getTranslationCallBack(index));
				fragment.updateTranslation();
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
	
	@Override
	public Translation getTranslationCallBack(int index){
		if(mAdapter == null && fragmentList != null){
			mAdapter = fragmentList.getAdapter();
		}
		if(mAdapter != null){
			if(mAdapter.getCount() > index){
				return mAdapter.getItem(index);
			}
		}
		return null;
	}
	
	public GlossaryReaderContract getDatabse(){
		return database;
	}

	@Override
	public void showKanjiDetail(JapaneseCharacter character) {
		japaneseCharacter  = character;
		System.out.println("character: "+character);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		if(findViewById(R.id.detail_fragment) != null){
			// two frames layout
			Log.i("MainActivity","Setting info fragment");
			DisplayCharacterInfo fragment = (DisplayCharacterInfo)fragmentManager.findFragmentByTag("displayCharacter");
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			if(fragment!= null && fragment.isDetached()){
				fragmentTransaction.replace(R.id.detail_fragment, fragment,"displayCharacter");
			}else{
				DisplayCharacterInfo displayCharacter = new DisplayCharacterInfo();
				fragmentTransaction.replace(R.id.detail_fragment, displayCharacter,"displayCharacter");
			}
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
			return;
		}
		
		DisplayCharacterInfo displayCharacter = new DisplayCharacterInfo();
		displayCharacter.setRetainInstance(true);
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(android.R.id.tabcontent, displayCharacter,"displayCharacter");
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public JapaneseCharacter getJapaneseCharacter() {
		return japaneseCharacter;
	}
	
    

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
