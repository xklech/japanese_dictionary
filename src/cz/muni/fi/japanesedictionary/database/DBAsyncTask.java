package cz.muni.fi.japanesedictionary.database;

import cz.muni.fi.japanesedictionary.entity.Translation;
import android.os.AsyncTask;


public class DBAsyncTask extends AsyncTask<Translation, Void, Void>{

	GlossaryReaderContract mDatabase;
	
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
