package cz.muni.fi.japanesedictionary.database;

import cz.muni.fi.japanesedictionary.entity.Translation;
import android.os.AsyncTask;

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
