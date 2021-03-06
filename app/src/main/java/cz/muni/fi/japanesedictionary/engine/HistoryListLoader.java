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
import android.util.Log;

import java.util.List;

import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.main.FavoriteActivity;

/**
 * AsyncTask for loading status of translations - favorit
 * @author Jaroslav Klech
 *
 */
public class HistoryListLoader extends AsyncTask<Void, Void, List<Translation>> {
    public static String LOG_TAG = "HistoryListLoader";

    private GlossaryReaderContract mDatabase;

    private TranslationsAdapter mAdapter;

    public HistoryListLoader(GlossaryReaderContract _database,TranslationsAdapter _adapter){
        mDatabase = _database;
        mAdapter = _adapter;
    }

    @Override
    protected List<Translation> doInBackground(Void... voids) {
        Log.i(LOG_TAG, "History loader started");
        if(mDatabase == null){
            Log.w(LOG_TAG, "Error loading list of last translations, database is null");
            return null;
        }
        return mDatabase.getLastTranslations(1000);
    }

    @Override
    protected void onPostExecute(List<Translation> translations) {
        if(translations != null && mAdapter != null){
            mAdapter.setData(translations);
        }

    }
}
