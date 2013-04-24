package cz.muni.fi.japanesedictionary.engine;

import java.util.List;

import cz.muni.fi.japanesedictionary.entity.Translation;

public interface SearchListener {

	public void onResultFound(Translation translation);
	
	public void onLoadFinished(	List<Translation> data);
	
}
