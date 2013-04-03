package cz.muni.fi.japanesedictionary.database;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import cz.muni.fi.japanesedictionary.main.DisplayTranslation;
import cz.muni.japanesedictionary.entity.Translation;


public class DBLastDisplayedTranslation extends AsyncTask<Void, Void, Translation>{

	GlossaryReaderContract database;
	DisplayTranslation fragment;
	private Context context;
	public DBLastDisplayedTranslation(GlossaryReaderContract _database,Context _context, DisplayTranslation _fragment){
		database = _database;
		context = _context;
		fragment = _fragment;
	}
	
	@Override
	protected Translation doInBackground(Void... params) {
		List<Translation> translations = database.getLastTranslations(1);
		if(translations != null && translations.size() > 0){
			return translations.get(0);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Translation result) {
		fragment.setTranslation(result);
		fragment.updateTranslation();
		super.onPostExecute(result);
	}
	
}
