package cz.muni.fi.japanesedictionary.entity;

import android.os.Bundle;

/**
 * TatoebaSentence
 */
public class TatoebaSentence {

    public static final String SAVE_TATOEBA_JAPANESE = "cz.muni.fi.japanesedictionary.SAVE_TATOEBA_JAPANESE";
    public static final String SAVE_TATOEBA_DUTCH = "cz.muni.fi.japanesedictionary.SAVE_TATOEBA_DUTCH";
    public static final String SAVE_TATOEBA_ENGLISH = "cz.muni.fi.japanesedictionary.SAVE_TATOEBA_ENGLISH";
    public static final String SAVE_TATOEBA_FRENCH = "cz.muni.fi.japanesedictionary.SAVE_TATOEBA_FRENCH";
    public static final String SAVE_TATOEBA_GERMAN = "cz.muni.fi.japanesedictionary.SAVE_TATOEBA_GERMAN";
    public static final String SAVE_TATOEBA_RUSSIAN = "cz.muni.fi.japanesedictionary.SAVE_TATOEBA_RUSSIAN";


    private String mJapaneseSentence;
    private String mEnglish;
    private String mFrench;
    private String mDutch;
    private String mGerman;
    private String mRussian;

    public String getJapaneseSentence() {
        return mJapaneseSentence;
    }

    public void setJapaneseSentence(String mJapaneseSentence) {
        this.mJapaneseSentence = mJapaneseSentence;
    }

    public String getEnglish() {
        return mEnglish;
    }

    public void setEnglish(String mEnglish) {
        this.mEnglish = mEnglish;
    }

    public String getFrench() {
        return mFrench;
    }

    public void setFrench(String mFrench) {
        this.mFrench = mFrench;
    }

    public String getDutch() {
        return mDutch;
    }

    public void setDutch(String mDutch) {
        this.mDutch = mDutch;
    }

    public String getGerman() {
        return mGerman;
    }

    public void setGerman(String mGerman) {
        this.mGerman = mGerman;
    }

    public String getRussian() {
        return mRussian;
    }

    public void setRussian(String mRussian) {
        this.mRussian = mRussian;
    }


    public Bundle convertToBundle(){
        Bundle bundle = new Bundle();
        bundle.putString(SAVE_TATOEBA_JAPANESE, mJapaneseSentence);
        bundle.putString(SAVE_TATOEBA_ENGLISH, mEnglish);
        bundle.putString(SAVE_TATOEBA_DUTCH, mDutch);
        bundle.putString(SAVE_TATOEBA_FRENCH, mFrench);
        bundle.putString(SAVE_TATOEBA_GERMAN, mGerman);
        bundle.putString(SAVE_TATOEBA_RUSSIAN, mRussian);
        return bundle;
    }

    public static TatoebaSentence createFromBundle(Bundle bundle){
        if(bundle == null){
            return null;
        }
        TatoebaSentence sentence = new TatoebaSentence();
        sentence.setJapaneseSentence(bundle.getString(SAVE_TATOEBA_JAPANESE));
        sentence.setEnglish(bundle.getString(SAVE_TATOEBA_ENGLISH));
        sentence.setDutch(bundle.getString(SAVE_TATOEBA_DUTCH));
        sentence.setFrench(bundle.getString(SAVE_TATOEBA_FRENCH));
        sentence.setGerman(bundle.getString(SAVE_TATOEBA_GERMAN));
        sentence.setRussian(bundle.getString(SAVE_TATOEBA_RUSSIAN));
        return sentence;
    }

}
