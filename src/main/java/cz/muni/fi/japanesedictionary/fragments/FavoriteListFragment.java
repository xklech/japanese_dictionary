package cz.muni.fi.japanesedictionary.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.engine.FavoriteListLoader;
import cz.muni.fi.japanesedictionary.engine.TranslationsAdapter;
import cz.muni.fi.japanesedictionary.main.AboutActivity;
import cz.muni.fi.japanesedictionary.main.FavoriteActivity;
import cz.muni.fi.japanesedictionary.main.MainActivity;
import cz.muni.fi.japanesedictionary.main.MyPreferencesActivity;

/**
 * Created by Jarek on 3.8.13.
 */
public class FavoriteListFragment extends ListFragment {

    TranslationsAdapter mAdapter;

    private static final String LOG_TAG = "FavoriteListFragment";

    private GlossaryReaderContract mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new TranslationsAdapter(getActivity());
        setListAdapter(mAdapter);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE );
        setEmptyText(getString(R.string.nothing_found));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "OnStart called - launching loader");
        if(mDatabase == null){
            mDatabase = new GlossaryReaderContract(getActivity());
        }
        mAdapter.clear();
        FavoriteListLoader loader = new FavoriteListLoader(mDatabase, mAdapter);
        loader.execute();
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle bundle = mAdapter.getItem(position).createBundleFromTranslation(null);
        Intent intent = new Intent(getActivity(),cz.muni.fi.japanesedictionary.main.DisplayTranslationActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        mDatabase.close();
        super.onDestroy();
    }

    /**
     * Returns TranslationAdapter from ListFragment
     * @return TranslationsAdapter adapter from list fragment
     */
    public TranslationsAdapter getAdapter(){
        return mAdapter;
    }
}
