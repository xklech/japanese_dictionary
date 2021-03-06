package cz.muni.fi.japanesedictionary.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.engine.HistoryListLoader;
import cz.muni.fi.japanesedictionary.engine.TranslationsAdapter;

/**
 * Created by Jarek on 3.8.13.
 */
public class HistoryListFragment extends ListFragment {

    TranslationsAdapter mAdapter;

    private static final String LOG_TAG = "HistoryListFragment";

    private GlossaryReaderContract mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new TranslationsAdapter(getActivity());
        setListAdapter(mAdapter);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setEmptyText(getString(R.string.nothing_found));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "OnStart called - launching loader");
        if (mDatabase == null) {
            mDatabase = new GlossaryReaderContract(getActivity());
        }
        mAdapter.clear();
        HistoryListLoader loader = new HistoryListLoader(mDatabase, mAdapter);
        loader.execute();
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle bundle = mAdapter.getItem(position).createBundleFromTranslation(null);
        Intent intent = new Intent(getActivity(), cz.muni.fi.japanesedictionary.main.DisplayTranslationActivity.class);
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
     *
     * @return TranslationsAdapter adapter from list fragment
     */
    public TranslationsAdapter getAdapter() {
        return mAdapter;
    }
}
