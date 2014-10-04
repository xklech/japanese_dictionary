package cz.muni.fi.japanesedictionary.interfaces;

import java.util.List;

import cz.muni.fi.japanesedictionary.entity.TatoebaSentence;

/**
 * TatoebaSentenceCallback
 */
public interface TatoebaSentenceCallback {

    public void receiveSentences(List<TatoebaSentence> sentences);

}
