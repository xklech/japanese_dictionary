package cz.muni.fi.japanesedictionary.engine;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.Translation;

/**
 * Created by Jarek on 21.7.13.
 */
public class FavoriteChanger extends AsyncTask<Translation,Void,Boolean> {

    public static String LOG_TAG = "FavoriteChanger";

    private GlossaryReaderContract mDatabase;

    private MenuItem mFavoriteItem;

    private Fragment mDisplayFragment;

    public FavoriteChanger(GlossaryReaderContract _database, MenuItem _menuItem, Fragment _displayFragment){
        mDatabase = _database;
        mFavoriteItem = _menuItem;
        mDisplayFragment = _displayFragment;
    }

    @Override
    protected Boolean doInBackground(Translation[] translations) {
        Log.i(LOG_TAG, "Favorite changer started");
        if(mDatabase == null){
            Log.w(LOG_TAG, "Error changing whether is favorite, database is null");
            return false;
        }
        Translation translation = translations[0];
        if(translation == null){
            Log.w(LOG_TAG, "Error changing whether is favorite, translation is null");
            return false;
        }
        return mDatabase.changeFavorised(translation);

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(mDisplayFragment != null && mDisplayFragment.isVisible()){
            if(mFavoriteItem != null){
                mFavoriteItem.setEnabled(true);
                mFavoriteItem.setVisible(true);
                Log.i(LOG_TAG, "Favorite changer setting");
                if(result){
                    mFavoriteItem.setIcon(R.drawable.rating_important);
                }else{
                    mFavoriteItem.setIcon(R.drawable.rating_not_important);
                }
            }
        }
    }



}
