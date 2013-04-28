package cz.muni.fi.japanesedictionary.interfaces;

import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;


public interface OnCreateTranslationListener{
	
	public GlossaryReaderContract getDatabse();
	
	public void showKanjiDetail(JapaneseCharacter character);
	
}