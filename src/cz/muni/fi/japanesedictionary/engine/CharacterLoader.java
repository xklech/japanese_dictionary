package cz.muni.fi.japanesedictionary.engine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.fi.japanesedictionary.fragments.DisplayTranslation;
import cz.muni.fi.japanesedictionary.parser.ParserService;

/**
 * AsyncTask loader for characters. Search for character info in background.
 * @author PC
 *
 */
public class CharacterLoader extends AsyncTask<String,Void,Map<String,JapaneseCharacter>>{

	private Context mContext;
	private IndexSearcher mSearcher;
	DisplayTranslation mFragment;
	
	/**
	 * Consturctor for CharacterLoader
	 * @param _context environment context
	 * @param _fragment Translation fragment to which characters are to be returned
	 */
	public CharacterLoader(Context _context, DisplayTranslation _fragment){
		mContext = _context;
		mFragment = _fragment;
	}
	
	
	/**
	 * Searchs for japanese characters in KanjiDict2.
	 * 
	 * @param params string which contains characters
	 * @return Map<String, JapaneseCharacter> if some characters were found returns map else null
	 */
	@Override
	protected Map<String, JapaneseCharacter> doInBackground(
			String... params) {
		String characterList = params[0];
		if(characterList == null || characterList.length() <1){
			return null;
		}
		SharedPreferences settings = mContext.getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
        String pathToDictionary = settings.getString("pathToKanjiDictionary", null);
        if(pathToDictionary == null){
        	Log.e("CharacterLoader", "No path to kanjidict2 dictionary");
        	return null;
        }
        File file = new File(pathToDictionary);
        if(file == null || !file.exists() || !file.canRead()){
        	Log.e("CharacterLoader", "Can't read dictionary directory");
        	return null;
        }
        StringBuffer searchBuilder = new StringBuffer();
        final int characterListSize = characterList.length();
        // search string
        for(int i =0;i< characterListSize ; i++){
        	String character = String.valueOf(characterList.charAt(i));
        	if(Pattern.matches("\\p{Han}", character)){
        		if(i > 0){ //searchBuilder.length() > 0
	        		searchBuilder.append(' '); // in lucene space serve as OR
	        	}
	        	searchBuilder.append('"' +  character + '"');
        	}
        }
        String search = searchBuilder.toString();
        if(search == null || search.length() == 0){
        	return null;
        }
        
        Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
        try{
        	QueryParser query = new QueryParser(Version.LUCENE_36, "literal", analyzer);
    		query.setPhraseSlop(0);

    		Query q = query.parse(search); 
	    	if( mSearcher == null){
	    		Directory dir = FSDirectory.open(file);
		    	IndexReader reader = IndexReader.open(dir);
		    	mSearcher= new IndexSearcher(reader);
	    	}
	    	TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
	    	mSearcher.search(q, collector);
	    	ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    	
	    	Map<String, JapaneseCharacter> result = new HashMap<String, JapaneseCharacter>();
	    	for(ScoreDoc document : hits){
	    		int docId = document.doc;
	    		Document d = mSearcher.doc(docId);

	    		JapaneseCharacter japanCharacter = new JapaneseCharacter();
	    	    String literal = d.get("literal");
	    	    if(literal != null && literal.length() > 0 ){
	    	    	japanCharacter.setLiteral(literal);
	    	    }
	    	    String radicalClassic = d.get("radicalClassic");
	    	    if(radicalClassic != null && radicalClassic.length() > 0 ){
	    	    	try{
	    	    		int radicalClassicInt = Integer.parseInt(radicalClassic);
	    	    		if(radicalClassicInt > 0){
	    	    			japanCharacter.setRadicalClassic(radicalClassicInt);
	    	    		}
	    	    	}catch(NumberFormatException ex){
	    	    		Log.w("CharacterLoader","Couldn't parse radical-classical: " + radicalClassic);
	    	    	}
	    	    }
	    	    String grade = d.get("grade");
	    	    if(grade != null && grade.length() > 0 ){
	    	    	try{
	    	    		int gradeInt = Integer.parseInt(grade);
	    	    		if(gradeInt > 0){
	    	    			japanCharacter.setGrade(gradeInt);
	    	    		}
	    	    	}catch(NumberFormatException ex){
	    	    		Log.w("CharacterLoader","Couldn't parse grade: " + grade);
	    	    	}
	    	    }
	    	    String strokeCount = d.get("strokeCount");
	    	    if(strokeCount != null && strokeCount.length() > 0 ){
	    	    	try{
	    	    		int strokeCountInt = Integer.parseInt(strokeCount);
	    	    		if(strokeCountInt > 0){
	    	    			japanCharacter.setStrokeCount(strokeCountInt);
	    	    		}
	    	    	}catch(NumberFormatException ex){
	    	    		Log.w("CharacterLoader","Couldn't parse strokeCount: " + strokeCount);
	    	    	}
	    	    }
	    	    
	    	    String skip = d.get("queryCodeSkip");
	    	    if(skip != null && skip.length() > 0 ){
	    	    	japanCharacter.setSkip(skip);
	    	    }
	    	    
	    	    String dicRef = d.get("dicRef");
	    	    if(dicRef != null && dicRef.length() > 0 ){
	    	    	japanCharacter.parseDicRef(dicRef);
	    	    }
	    		
	    	    String rmGroupJaOn = d.get("rmGroupJaOn");
	    	    if(rmGroupJaOn != null && rmGroupJaOn.length() > 0 ){
	    	    	japanCharacter.parseRmGroupJaOn(rmGroupJaOn);
	    	    }
	    	    
	    	    String rmGroupJaKun = d.get("rmGroupJaKun");
	    	    if(rmGroupJaKun != null && rmGroupJaKun.length() > 0 ){
	    	    	japanCharacter.parseRmGroupJaKun(rmGroupJaKun);
	    	    }
	    	    
	    	    String meaningEnglish = d.get("meaningEnglish");
	    	    if(meaningEnglish != null && meaningEnglish.length() > 0 ){
	    	    	japanCharacter.parseMeaningEnglish(meaningEnglish);
	    	    }
	    	    
	    	    String meaningFrench = d.get("meaningFrench");
	    	    if(meaningFrench != null && meaningFrench.length() > 0 ){
	    	    	japanCharacter.parseMeaningFrench(meaningFrench);
	    	    }
	    	    
	    	    String meaningDutch = d.get("meaningDutch");
	    	    if(meaningDutch != null && meaningDutch.length() > 0 ){
	    	    	japanCharacter.parseMeaningDutch(meaningDutch);
	    	    }
	    	    
	    	    String meaningGerman = d.get("meaningGerman");
	    	    if(meaningGerman != null && meaningGerman.length() > 0 ){
	    	    	japanCharacter.parseMeaningGerman(meaningGerman);
	    	    }
	    	    
	    	    String nanori = d.get("nanori");
	    	    if(nanori != null && nanori.length() > 0 ){
	    	    	japanCharacter.parseNanori(nanori);
	    	    }
	    	    if(japanCharacter.getLiteral() != null && japanCharacter.getLiteral().length() > 0){
	    	    	result.put(japanCharacter.getLiteral(), japanCharacter);
	    	    }
	    	}
	    	return result.size() > 0 ? result : null;
	    	
        }catch(ParseException ex){
        	Log.e("CharacterLoader","Searching for charaters ParseException caught: "+ex);
        }catch(IOException ex){
        	Log.e("CharacterLoader","Searching for charaters IOException caught: "+ex);
        }catch(Exception ex){
        	Log.e("CharacterLoader","Searching for charaters Exception caught: "+ex);
        }
        

		return null;
	}
	
	/**
	 * Called after loading is done. Runs in UI thread and sets characters to fragment.
	 */
	@Override
	protected void onPostExecute(Map<String, JapaneseCharacter> result) {
		mFragment.setCharacters(result);
		
		super.onPostExecute(result);
	}
	

	
}
