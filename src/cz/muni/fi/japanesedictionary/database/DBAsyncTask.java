package cz.muni.fi.japanesedictionary.database;

import cz.muni.fi.japanesedictionary.entity.Translation;
import android.os.AsyncTask;


public class DBAsyncTask extends AsyncTask<Translation, Void, Void>{

	GlossaryReaderContract database;
	
	public DBAsyncTask(GlossaryReaderContract _database){
		database = _database;
	}
	
	@Override
	public Void doInBackground(Translation... params) {
		Translation translation = params[0];
		if(translation == null){
			return null;
		}
		database.saveTranslation(translation);
		
		return null;
	}

}
