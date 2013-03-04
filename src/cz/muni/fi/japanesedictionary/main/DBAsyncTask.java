package cz.muni.fi.japanesedictionary.main;

import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import android.os.AsyncTask;


public class DBAsyncTask extends AsyncTask<Translation, Void, Void>{

	GlossaryReaderContract database;
	
	public DBAsyncTask(GlossaryReaderContract _database){
		database = _database;
	}
	
	@Override
	protected Void doInBackground(Translation... params) {
		Translation translation = params[0];
		if(translation == null){
			return null;
		}
		database.saveTranslation(translation);
		
		return null;
	}

}
