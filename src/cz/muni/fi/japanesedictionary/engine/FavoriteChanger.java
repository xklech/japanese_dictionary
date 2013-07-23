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
package cz.muni.fi.japanesedictionary.engine;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.Translation;

/**
 * AsyncTask for changing status of translation - favorit
 * @author Jaroslav Klech
 *
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
        return mDatabase.changeFavorite(translation);

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(mDisplayFragment != null && !mDisplayFragment.isDetached()){
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
