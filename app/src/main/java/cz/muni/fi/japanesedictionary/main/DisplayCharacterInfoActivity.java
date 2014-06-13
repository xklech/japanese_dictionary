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
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.engine.DrawerAdapter;
import cz.muni.fi.japanesedictionary.engine.DrawerItemClickListener;
import cz.muni.fi.japanesedictionary.entity.DrawerItem;
import cz.muni.fi.japanesedictionary.fragments.DisplayCharacterInfo;

public class DisplayCharacterInfoActivity extends ActionBarActivity
		{
	private static final String LOG_TAG = "DisplayCharacterInfoActivity";


    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG,"Setting layout");
		setContentView(R.layout.display_activity);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);



        // Set the adapter for the list view
        List<DrawerItem> drawerItems = new ArrayList<>();
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
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                Log.i(LOG_TAG, "Drawer change - open");
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener(this));


		if(savedInstanceState != null){
			return ;
		}
		Bundle bundle = getIntent().getExtras();

		DisplayCharacterInfo displayCharacter= new DisplayCharacterInfo();
		displayCharacter.setArguments(bundle);
		displayCharacter.setRetainInstance(true);
		
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Log.i(LOG_TAG,"Setting main fragment");
		ft.add(R.id.display_fragment_container, displayCharacter);
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
        mDrawerLayout.closeDrawer(mDrawerList);
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

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

    @Override
    protected void onStop() {
        mDrawerLayout.closeDrawer(mDrawerList);
        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
