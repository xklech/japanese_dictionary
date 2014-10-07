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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;


import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;


import cz.muni.fi.japanesedictionary.Const;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.engine.DrawerAdapter;
import cz.muni.fi.japanesedictionary.engine.MainPagerAdapter;
import cz.muni.fi.japanesedictionary.engine.TranslationsAdapter;
import cz.muni.fi.japanesedictionary.entity.DrawerItem;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.fi.japanesedictionary.entity.TatoebaSentence;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.fragments.DictionaryFragmentAlertDialog;
import cz.muni.fi.japanesedictionary.fragments.DisplayCharacterInfo;
import cz.muni.fi.japanesedictionary.fragments.DisplaySentenceInfo;
import cz.muni.fi.japanesedictionary.fragments.DisplayTranslation;
import cz.muni.fi.japanesedictionary.fragments.ResultFragmentList;
import cz.muni.fi.japanesedictionary.interfaces.OnCreateTranslationListener;
import cz.muni.fi.japanesedictionary.interfaces.OnTranslationSelectedListener;
import cz.muni.fi.japanesedictionary.parser.ParserService;
import cz.muni.fi.japanesedictionary.util.MiscellaneousUtil;


/**
 * Main Activity for JapaneseDictionary. Works with all fragments.
 * @author Jaroslav Klech
 *
 */
public class MainActivity extends ActionBarActivity
        implements OnCreateTranslationListener,
        OnTranslationSelectedListener,
        SearchView.OnQueryTextListener,
        ActionBar.TabListener

{

    private static final String LOG_TAG = "MainActivity";

    public static final String DUAL_PANE = "cz.muni.fi.japanesedictionary.mainactivity.dualpane";
    public static final String PARSER_SERVICE = "cz.muni.fi.japanesedictionary.parser.ParserService";
    public static final String SEARCH_TEXT = "cz.muni.fi.japanesedictionary.edit_text_searched";
    public static final String PART_OF_TEXT = "cz.muni.fi.japanesedictionary.edit_text_part";

    private GlossaryReaderContract mDatabase = null;

    private SearchView mSearchView;

    private String mLastTabId;
    private String mCurFilter;

    public static String[] mTabKeys = {"exact","beginning","middle","end"};

    private Menu mMenu;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mNoteVisible;
    private boolean mFavoriteVisible;
    private boolean mSearchVisible;

    private ViewPager mPager;

    private boolean mWaitingForConnection = false;

    private BroadcastReceiver mInternetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (!noConnectivity) {
                SharedPreferences settings = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
                String dictionaryPath = settings.getString(Const.PREF_JMDICT_PATH , null);
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
        supportRequestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.main_activity);
        setSupportProgressBarIndeterminate(true);
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
        mPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.viewPager_margin));


        if(mLastTabId == null || mLastTabId.length()==0){
            mLastTabId = "exact";
        }
        setUpTabs(mLastTabId);

        SharedPreferences settings = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
        String dictionaryPath = settings.getString(Const.PREF_JMDICT_PATH, null);
        boolean waitingForConnection = settings.getBoolean("waitingForConnection", false);
        if(waitingForConnection && (dictionaryPath == null || !(new File(dictionaryPath)).exists()) ) {
            displayDownloadPrompt();
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);



        // Set the adapter for the list view
        List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
        DrawerItem search = new DrawerItem().setName(getString(R.string.actionbar_search)).setIconResource(android.R.drawable.ic_menu_search);
        drawerItems.add(search);
        drawerItems.add(new DrawerItem().setName(getString(R.string.menu_favorite_activity)).setIconResource(R.drawable.rating_favorite));
        drawerItems.add(new DrawerItem().setName(getString(R.string.last_seen)).setIconResource(R.drawable.collections_view_as_list));
        DrawerAdapter drawerAdapter = new DrawerAdapter(getApplicationContext());
        drawerAdapter.setData(drawerItems);

        mDrawerList.setAdapter(drawerAdapter);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                Log.i(LOG_TAG, "Drawer change - close");
                if(!ActivityCompat.invalidateOptionsMenu(MainActivity.this)){
                    onPrepareOptionsMenu(mMenu);
                } // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                Log.i(LOG_TAG, "Drawer change - open");
                if(!ActivityCompat.invalidateOptionsMenu(MainActivity.this)){
                    onPrepareOptionsMenu(mMenu);
                } // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListenerMainActivity());


        Log.i(LOG_TAG,"Checking saved instance");
        if(savedInstanceState != null){
            return;
        }

        mSearchVisible = true;

        Log.i(LOG_TAG,"Setting layout");

        if(findViewById(R.id.detail_fragment) != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            // two frames layout
            Log.i(LOG_TAG,"Setting info fragment");
            DisplayTranslation displayTranslation = new DisplayTranslation();
            ft.add(R.id.detail_fragment, displayTranslation,"displayFragment");
            ft.commit();
        }

        if (dictionaryPath == null || !(new File(dictionaryPath)).exists() ) {
            displayDownloadPrompt();
        }


    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        MenuItem noteItem = menu.findItem(R.id.ab_note);
        MenuItem favoriteItem = menu.findItem(R.id.favorite);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(!drawerOpen);
        }

        if(drawerOpen){
            //is open
            Log.i(LOG_TAG, "Drawer open");
            mNoteVisible = noteItem != null && noteItem.isVisible();
            mFavoriteVisible = favoriteItem != null && favoriteItem.isVisible();
            mSearchVisible = MenuItemCompat.isActionViewExpanded(searchItem);
            if (noteItem != null) {
                noteItem.setVisible(false);
            }
            if (favoriteItem != null) {
                favoriteItem.setVisible(false);
            }
            Log.i(LOG_TAG, "Drawer open - hide search");
            MenuItemCompat.collapseActionView(searchItem);
            if (searchItem != null) {
                searchItem.setVisible(false);
                searchItem.setEnabled(false);
            }

        }else{
            Log.i(LOG_TAG, "Drawer close");
            if (favoriteItem != null) {
                favoriteItem.setVisible(mFavoriteVisible);
            }
            if (noteItem != null) {
                noteItem.setVisible(mNoteVisible);
            }
            if(searchItem != null){
                searchItem.setVisible(true);
                searchItem.setEnabled(true);
            }
            if(mSearchVisible){
                Log.i(LOG_TAG, "Drawer close, expand search");
                MenuItemCompat.expandActionView(searchItem);
                mSearchView.setQuery(mCurFilter, false);
            }
        }


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        Log.i(LOG_TAG, "Inflating menu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        Log.i(LOG_TAG, "Setting menu ");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Place an action bar item for searching.
        MenuItem searchItem = menu.findItem(R.id.action_search);

        MenuItemCompat.expandActionView(searchItem);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconifiedByDefault(false);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        Log.i(LOG_TAG, "Setting query");
        mSearchView.setQuery(mCurFilter, false);
        Log.i(LOG_TAG, "Setting query done");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(LOG_TAG, "Configuration changed");
        if(mSearchVisible){
            MenuItem searchItem = mMenu.findItem(R.id.action_search);
            if(searchItem != null){
                Log.i(LOG_TAG, "Search item expanded");
                MenuItemCompat.expandActionView(searchItem);

                if(mSearchView != null){
                    Log.i(LOG_TAG, "text set");
                    mSearchView.setQuery(mCurFilter,true);
                }
            }

        }
        mDrawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
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
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        mDrawerLayout.closeDrawer(mDrawerList);
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        ResultFragmentList fragmentList = (ResultFragmentList) getSupportFragmentManager().findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
        if(fragmentList != null){
            fragmentList.updateList();
        }
        super.onResume();
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
        Log.i(LOG_TAG, "onquerychanged fired");

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
            DialogFragment newFragment = DictionaryFragmentAlertDialog.newInstance(
                    R.string.internet_connection_failed_title,
                    R.string.internet_connection_failed_message, true);
            newFragment.show(getSupportFragmentManager(), "dialog");
        } else if (!canWriteExternalStorage()) {
            DialogFragment newFragment = DictionaryFragmentAlertDialog.newInstance(
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
        Log.i(LOG_TAG, "Launching preference Activity");
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
    public GlossaryReaderContract getDatabase(){
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
        if(character == null){
            return;
        }
        Bundle bundle = character.createBundleFromJapaneseCharacter(null);

        FragmentManager fragmentManager = getSupportFragmentManager();
        DisplayCharacterInfo displayCharacter = new DisplayCharacterInfo();
        displayCharacter.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(container, displayCharacter, "displayCharacter");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * CallBack method for DisplayTranslation fragment. Launches new DisplaySentenceInfo fragment.
     *
     * @param sentence TatoebaSentence to be displayed
     */
    @Override
    public void showSentenceDetail(TatoebaSentence sentence) {
        Log.i(LOG_TAG,"Setting DisplayCharacterInfo fragment");

        // decides whether using two pane layout or replace fragment list
        final int container = R.id.detail_fragment;
        if(sentence == null){
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        DisplaySentenceInfo displaySentence = DisplaySentenceInfo.newInstance(sentence);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(container, displaySentence, "displaySentence");
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
        switch (selectedPart) {
            case "exact":
                selectedTab[0] = true;
                break;
            case "beginning":
                selectedTab[1] = true;
                break;
            case "middle":
                selectedTab[2] = true;
                break;
            default:
                selectedTab[3] = true;
                break;
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
            DialogFragment newFragment = DictionaryFragmentAlertDialog.newInstance(
                    R.string.no_dictionary_found,
                    R.string.download_dictionary_question, false);
            newFragment.setCancelable(false);
            newFragment.show(getSupportFragmentManager(),
                    "dialog");
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        if(intent == null){
            return ;
        }
        Log.i(LOG_TAG, "Voice search intent recognized");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(LOG_TAG, "Voice search intent recognized"+query);
            MenuItem searchItem = mMenu.findItem(R.id.action_search);

            MenuItemCompat.expandActionView(searchItem);
            mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            mSearchView.setQuery(query, true);
            ResultFragmentList fragmentList = (ResultFragmentList) getSupportFragmentManager().findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
            mCurFilter = query;
            fragmentList.search(mCurFilter);

        }

        Bundle bundle = intent.getExtras();
        if(bundle != null){
            Log.i(LOG_TAG, "Bundle: "+bundle);
            if(bundle.containsKey("search")){
                mSearchVisible = bundle.getBoolean("search", false);
                if(!mSearchVisible){

                    ResultFragmentList fragmentList = (ResultFragmentList) getSupportFragmentManager().findFragmentByTag(getFragmentTag(mPager.getCurrentItem()));
                    if(fragmentList != null){
                        fragmentList.search(null);
                    }
                }
            }
        }
        super.onNewIntent(intent);
    }

    private class DrawerItemClickListenerMainActivity implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            switch(position){
                case 0:
                    Log.i(LOG_TAG, "Launching Favorite activity - search");
                    MenuItem searchItem = mMenu.findItem(R.id.action_search);
                    MenuItemCompat.expandActionView(searchItem);
                    mDrawerLayout.closeDrawer(mDrawerList);
                    break;
                case 1:
                    Log.i(LOG_TAG, "Launching Favorite activity");
                    mDrawerLayout.closeDrawer(mDrawerList);
                    Intent intentFavorites = new Intent(MainActivity.this,FavoriteActivity.class);
                    intentFavorites.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentFavorites);
                    break;
                case 2:
                    Log.i(LOG_TAG, "Launching HistoryActivity - last 1000");
                    mDrawerLayout.closeDrawer(mDrawerList);
                    Intent intentSetting = new Intent(MainActivity.this,HistoryActivity.class);
                    intentSetting.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentSetting);
                    break;
            }
        }
    }




}
