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
import android.view.MenuItem;

import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.fragments.DisplayTranslation;

/**
 * AsyncTask loader for notes.
 * @author Jaroslav Klech
 *
 */
public class NoteLoader extends AsyncTask<Translation,Void,String> {

    public static String LOG_TAG = "NoteLoader";

    private GlossaryReaderContract mDatabase;

    private MenuItem mNoteItem;

    private DisplayTranslation mDisplayFragment;

    public NoteLoader(GlossaryReaderContract _database, MenuItem _noteItem, DisplayTranslation _displayFragment){
        mDatabase = _database;

        mNoteItem = _noteItem;
        mDisplayFragment = _displayFragment;
    }

    @Override
    protected String doInBackground(Translation[] translations) {
        Log.i(LOG_TAG, "NoteLoader started");
        if(mDatabase == null){
            Log.w(LOG_TAG, "Error NoteLoader, database is null");
            return null;
        }
        Translation translation = translations[0];
        if(translation == null){
            Log.w(LOG_TAG, "Error NoteLoader, translation is null");
            return null;
        }
        return mDatabase.getNote(translation);

    }

    @Override
    protected void onPostExecute(String result) {
        if(mDisplayFragment != null && mDisplayFragment.isVisible() ){
            if(mNoteItem != null){
                mNoteItem.setEnabled(true);
                mNoteItem.setVisible(true);
                Log.i(LOG_TAG, "Note loader setting");
            }
            if(result == null || result.length() == 0){
                mDisplayFragment.displayNote(null);
            }else{
                mDisplayFragment.displayNote(result);
            }

        }
    }

}
