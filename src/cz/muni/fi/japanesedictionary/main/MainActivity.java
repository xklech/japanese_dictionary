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
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.parser.ParserService;
import cz.muni.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.japanesedictionary.entity.Translation;

public class MainActivity extends SherlockFragmentActivity
	implements ResultFragmentList.OnTranslationSelectedListener,
				DisplayTranslation.OnCreateTranslationListener,
				DisplayCharacterInfo.OnLoadGetCharacterListener{
	
	public static final String PARSER_SERVICE = "cz.muni.fi.japanesedictionary.parser.ParserService";
	public static final String SEARCH_PREFERENCES = "cz.muni.fi.japanesedictionary.main.search_preferences";
	public static final String SEARCH_TEXT = "cz.muni.fi.japanesedictionary.edit_text_searched";
	public static final String PART_OF_TEXT = "cz.muni.fi.japanesedictionary.edit_text_part";
	public static final String HANDLER_BUNDLE_TRANSLATION = "cz.muni.fi.japanesedictionary.handler_bundle_translation";
	public static final String HANDLER_BUNDLE_TAB = "cz.muni.fi.japanesedictionary.handler_bundle_tab";
	public static final String FRAGMENT_CREATE_TRANSLATION = "cz.muni.fi.japanesedictionary.fragment_create_translation";
	public static final String FRAGMENT_CREATE_PART = "cz.muni.fi.japanesedictionary.fragment_create_part";
	
	
	private MainFragment mainFragment;
	private TranslationsAdapter mAdapter = null;
	private GlossaryReaderContract database = null;
	private JapaneseCharacter japaneseCharacter;
	public void setAdapter(TranslationsAdapter _adapter){
		mAdapter = _adapter;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("MainActivity","Checking saved instance");
		setContentView(R.layout.main_activity);
		database = new GlossaryReaderContract(getApplicationContext());
		if(savedInstanceState != null){
			return ;
		}
		Log.i("MainActivity","Setting layout");
		mainFragment = new MainFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Log.i("MainActivity","Setting main fragment");
		ft.add(R.id.main_fragment, mainFragment,"mainFragment");
		if(findViewById(R.id.detail_fragment) != null){
			// two frames layout
			Log.i("MainActivity","Setting info fragment");
			DisplayTranslation displayTranslation = new DisplayTranslation();
			ft.add(R.id.detail_fragment, displayTranslation,"displayFragment");
		}

		ft.commit();
	}

	@Override
	protected void onDestroy() {
		database.close();
		super.onDestroy();
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
			fragment.setTranslation(getTranslationCallBack(index));
			fragment.updateTranslation();
			return;
		}
		DisplayTranslation displayFragment = new DisplayTranslation();
		Bundle bundle = new Bundle();
		bundle.putInt("TranslationId", index);
		displayFragment.setArguments(bundle);
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.main_fragment, displayFragment,"displayFragment");
		ft.addToBackStack(null);
		ft.commit();
		
	}
	
	@Override
	public Translation getTranslationCallBack(int index){
		if(mAdapter != null){
			return mAdapter.getItem(index);
		}
		return null;
	}
	
	public GlossaryReaderContract getDatabse(){
		return database;
	}

	@Override
	public void showKanjiDetail(JapaneseCharacter character) {
		// TODO Auto-generated method stub
		japaneseCharacter  = character;
		System.out.println(japaneseCharacter);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		if(findViewById(R.id.detail_fragment) != null){
			// two frames layout
			Log.i("MainActivity","Setting info fragment");
			DisplayTranslation fragment = (DisplayTranslation)fragmentManager.findFragmentByTag("displayFragment");
			
			return;
		}
		
		DisplayCharacterInfo displayCharacter = new DisplayCharacterInfo();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main_fragment, displayCharacter,"displayCharacter");
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public JapaneseCharacter getJapaneseCharacter() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
