package cz.muni.fi.japanesedictionary.main;

import android.support.v4.app.Fragment;
import cz.muni.fi.japanesedictionary.R;

public class Languages {
	
	private Fragment context;
	
	private String[] languages;
	
	public Languages(Fragment con){
		this.context = con;
		languages = new String[]{
				context.getString(R.string.language_english),
				context.getString(R.string.language_french),
				context.getString(R.string.language_dutch),
				context.getString(R.string.language_german)
			};
	}
	
	public String[] getLanguages(){
		return languages;
	}
	
	public String getLanguage(int id){
		return languages[id];
	}

}
