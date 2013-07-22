package cz.muni.fi.japanesedictionary.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.engine.FavoriteListLoader;
import cz.muni.fi.japanesedictionary.engine.TranslationsAdapter;

/**
 * Created by Jarek on 22.7.13.
 */
public class FavoriteActivity extends SherlockListActivity {
    TranslationsAdapter mAdapter;
    private static final String LOG_TAG = "FavoriteActivity";

    private GlossaryReaderContract mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_activity);
        mAdapter = new TranslationsAdapter(getApplicationContext());
        setListAdapter(mAdapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE );
        TextView empty = (TextView) getListView().getEmptyView();
        empty.setText(R.string.nothing_found);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_details, menu);
        Log.i(LOG_TAG, "Setting menu ");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        menu.findItem(R.id.favorites_activity).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onResume() {
        Log.i(LOG_TAG, "OnStart called - launching loader");
        if(mDatabase == null){
            mDatabase = new GlossaryReaderContract(this);
        }
        mAdapter.clear();
        FavoriteListLoader loader = new FavoriteListLoader(mDatabase, mAdapter, this);
        loader.execute();
        super.onResume();
    }

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
                Log.i(LOG_TAG, "Launching preference Activity");
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Bundle bundle = mAdapter.getItem(position).createBundleFromTranslation(null);
        Intent intent = new Intent(this.getApplicationContext(),cz.muni.fi.japanesedictionary.main.DisplayTranslationActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
