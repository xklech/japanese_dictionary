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

package cz.muni.fi.japanesedictionary.database;

import android.os.AsyncTask;

import cz.muni.fi.japanesedictionary.entity.Translation;

/**
 * Class for saving Translation to database in bakcground
 * @author Jaroslav Klech
 */
public class DBAsyncTask extends AsyncTask<Translation, Void, Void>{

	private GlossaryReaderContract mDatabase;
	
	public DBAsyncTask(GlossaryReaderContract _database){
		mDatabase = _database;
	}
	
	@Override
	public Void doInBackground(Translation... params) {
		Translation translation = params[0];
		if(translation == null){
			return null;
		}

		mDatabase.saveTranslation(translation);
		
		return null;
	}

}
