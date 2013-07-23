package cz.muni.fi.japanesedictionary.engine;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.main.FavoriteActivity;

/**
 * Created by Jarek on 22.7.13.
 */
public class FavoriteListLoader extends AsyncTask<Void, Void, List<Translation>> {
    public static String LOG_TAG = "FavoriteListLoader";

    private GlossaryReaderContract mDatabase;

    private TranslationsAdapter mAdapter;

    private FavoriteActivity mActivity;

    public FavoriteListLoader(GlossaryReaderContract _database,TranslationsAdapter _adapter, FavoriteActivity _activity){
        mDatabase = _database;
        mAdapter = _adapter;
        mActivity = _activity;
    }

    @Override
    protected List<Translation> doInBackground(Void... voids) {
        Log.i(LOG_TAG, "Favorite loader started");
        if(mDatabase == null){
            Log.w(LOG_TAG, "Error loading list of favorite, database is null");
            return null;
        }
        return mDatabase.selectFavoriteTranslations();
    }

    @Override
    protected void onPostExecute(List<Translation> translations) {
        if(translations != null && mAdapter != null){
            mAdapter.setData(translations);
        }

    }
}
