package cz.muni.fi.japanesedictionary.entity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jaroslav
 */
public enum TatoebaLangEnum {
    JAPANESE,
    ENGLISH,
    FRENCH,
    DUTCH,
    DEUTSCH ,
    RUSSIAN;
    /**
     *                 public static final String JAPANESE = "jpn";
     public static final String ENGLISH = "eng";
     public static final String FRENCH = "fra";
     public static final String DUTCH = "nld";
     public static final String DEUTSCH = "deu";
     public static final String RUSSIAN = "rus";
     */


    public static final String JAPANESE_CODE = "jpn";
    public static final String ENGLISH_CODE = "eng";
    public static final String FRENCH_CODE = "fra";
    public static final String DUTCH_CODE = "nld";
    public static final String DEUTSCH_CODE = "deu";
    public static final String RUSSIAN_CODE = "rus";
    private static final Map<TatoebaLangEnum, String> mMapLanguages;
    static{
        mMapLanguages = new HashMap<>();
        mMapLanguages.put(JAPANESE, JAPANESE_CODE);
        mMapLanguages.put(ENGLISH, ENGLISH_CODE);
        mMapLanguages.put(FRENCH, FRENCH_CODE);
        mMapLanguages.put(DUTCH, DUTCH_CODE);
        mMapLanguages.put(DEUTSCH, DEUTSCH_CODE);
        mMapLanguages.put(RUSSIAN, RUSSIAN_CODE);
    }

    public static  String getLanguage(TatoebaLangEnum code){
        return mMapLanguages.get(code);
    }

    public static Set<String> getAll(){
        Set<String> languages = new HashSet<>();
        languages.add(getLanguage(JAPANESE));
        languages.add(getLanguage(ENGLISH));
        languages.add(getLanguage(FRENCH));
        languages.add(getLanguage(DUTCH));
        languages.add(getLanguage(DEUTSCH));
        languages.add(getLanguage(RUSSIAN));
        return languages;
    }



    public static Set<String> getWithoutJap(){
        Set<String> languages = getAll();
        languages.remove(getLanguage(JAPANESE));
        return languages;
    }



}
