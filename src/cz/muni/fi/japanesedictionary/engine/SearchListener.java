package cz.muni.fi.japanesedictionary.engine;

import cz.muni.fi.japanesedictionary.entity.Translation;

public interface SearchListener {

	public void onResultFound(Translation translation);
	
}
