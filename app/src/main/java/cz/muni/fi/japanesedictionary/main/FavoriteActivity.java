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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
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
import cz.muni.fi.japanesedictionary.fragments.FavoriteListFragment;


/**
 * ListActivity displaying favorite translations
 *
 * @author Jaroslav Klech
 */
public class FavoriteActivity extends AppCompatActivity {

    private static final String LOG_TAG = "FavoriteActivity";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private FavoriteListFragment mFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_activity);
        mFragment = new FavoriteListFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.favorite_fragment, mFragment).commit();


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
                 R.string.drawer_open, R.string.drawer_close) {

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

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawer(mDrawerList);
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.settings:
                Log.i(LOG_TAG, "Lauching preference Activity");
                Intent intentSetting = new Intent(this.getApplicationContext(), cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
                intentSetting.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentSetting);
                return true;
            case R.id.about:
                Log.i(LOG_TAG, "Lauching About Activity");
                Intent intentAbout = new Intent(this.getApplicationContext(), cz.muni.fi.japanesedictionary.main.AboutActivity.class);
                intentAbout.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentAbout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


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

    @Override
    protected void onResume() {

        if (mFragment != null && mFragment.getAdapter() != null) {
            mFragment.getAdapter().updateAdapter();
        }
        super.onResume();
    }

}
