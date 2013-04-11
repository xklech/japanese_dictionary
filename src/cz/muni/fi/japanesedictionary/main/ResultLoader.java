package cz.muni.fi.japanesedictionary.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
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
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.parser.ParserService;
import cz.muni.fi.japanesedictionary.parser.RomanizationEnum;

public class ResultLoader extends AsyncTaskLoader<List<Translation>>{
	private Context mContext;
	private String mExpression = null;
	private String mPart;
	private IndexSearcher mSearcher;

	private String mLastSearched = null;
	private String mLastPart = null;
	
	private List<Translation> mLastTranslations = null;
	
	public ResultLoader(Context cont,String expr,String _part) {
		super(cont);
		mContext = cont;
		mPart = _part;
		mExpression = expr;		
	}

	@Override
	public List<Translation> loadInBackground() {
		
        SharedPreferences settings = mContext.getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
        boolean validDictionary = settings.getBoolean("hasValidDictionary", false);
        String pathToDictionary = settings.getString("pathToDictionary", null);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean englishBool = sharedPrefs.getBoolean("language_english", false);
        boolean frenchBool = sharedPrefs.getBoolean("language_french", false);        
        boolean dutchBool = sharedPrefs.getBoolean("language_dutch", false);
        boolean germanBool = sharedPrefs.getBoolean("language_german", false);
        List<Translation> translations = new ArrayList<Translation>();
        
        if(!validDictionary){
        	Log.e("ResultLoader", "No jmdict dictionary found");
        	return null;
        }
        if(pathToDictionary == null){
        	Log.e("ResultLoader", "No path to jmdict dictionary");
        	return null;
        }
        File file = new File(pathToDictionary);
        if(file == null || !file.canRead()){
        	Log.e("ResultLoader", "Cant read jmdict dictionary directory");
        	return null;
        }
        
        if((mExpression == null && mLastSearched == null && mLastTranslations != null) || ((mLastSearched != null && mLastSearched.equals(mExpression)) && (mLastPart != null && mLastPart.equals(mPart)))){
        	Log.i("ResultLoader","Search and part are the same, return old translation list");
        	return mLastTranslations;
        }
        
        if(mExpression == null){
        	//spusteni bez vyhledani
        	Log.i("ResultLoader","First run - last 10 translations ");
        	GlossaryReaderContract database = new GlossaryReaderContract(mContext);
        	translations = database.getLastTranslations(10);
        	database.close();
        	mLastTranslations = translations;
        	return translations;

        }
        if(mExpression.length() < 1){
        	Log.w("ResultLoader", "No expression to translate");
        	return null;
        }
        

        
		
    	Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
    	try{
    		QueryParser query = new QueryParser(Version.LUCENE_36, "japanese", analyzer);
    		query.setPhraseSlop(0);
    		String search;    		
    		
    		if(Pattern.matches("\\w*", mExpression)){
    			//only romaji
    			Log.i("ResultLoader","Only letters, converting to hiragana. ");
    			mExpression = RomanizationEnum.Hepburn.toHiragana(mExpression);
    		}
    		if("end".equals(mPart)){
    			search = "\""+mExpression + " lucenematch\"";
    		}else if("begining".equals(mPart)){
    			search = "\"lucenematch " + mExpression+"\"";	
    		}else if("middle".equals(mPart)){
    			search = mExpression;
    		}else {
    			search = "\"lucenematch "+mExpression + " lucenematch\"";
    		}
    		Log.i("ResultLoader"," Searching for: "+search);
    		Query q = query.parse(search); 
	    	 if( mSearcher == null){
	 	    	Directory dir = FSDirectory.open(file);
		    	IndexReader reader = IndexReader.open(dir);
		    	mSearcher= new IndexSearcher(reader);
	    	 }
	    	TopScoreDocCollector collector = TopScoreDocCollector.create(1000, true);
	    	mSearcher.search(q, collector);
	    	ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    	Log.i("ResultLoader", "Found: "+String.valueOf(hits.length)+" hits");
	    	for(int i=0;i<hits.length;++i) {
	    	    int docId = hits[i].doc;
	    	    Document d = mSearcher.doc(docId);
	    	    
	    	    Translation translation = new Translation();
	    	    String japanese_keb = d.get("japanese_keb");
	    	    if(japanese_keb != null && japanese_keb.length()!=0 ){
	    	    	translation.parseJapaneseKeb(japanese_keb);
	    	    }
	    	    
	    	    String japanese_reb = d.get("japanese_reb");
	    	    if(japanese_reb != null && japanese_reb.length()!=0 ){
	    	    	translation.parseJapaneseReb(japanese_reb);
	    	    }
	    	    
	    	    String english = d.get("english");
	    	    if(english != null && english.length()!=0 ){
	    	    	translation.parseEnglish(english);
	    	    }
	    	    
	    	    String french = d.get("french");
	    	    if(french != null && french.length()!=0 ){
	    	    	translation.parseFrench(french);
	    	    }
	    	    
	    	    String dutch = d.get("dutch");
	    	    if(dutch != null && dutch.length()!=0 ){
	    	    	translation.parseDutch(dutch);
	    	    } 	    	    
	    	    
	    	    String german = d.get("german");
	    	    if(german != null && german.length()!=0 ){
	    	    	translation.parseGerman(german);
	    	    }
	    	    
	    	    if(
	    	    	(englishBool && translation.getEnglishSense()!= null) || 
	    	    	(dutchBool && translation.getDutchSense()!= null) || 
	    	    	(germanBool && translation.getGermanSense()!= null) || 	
	    	    	(frenchBool && translation.getFrenchSense()!= null) ||
	    	    	(!englishBool && !dutchBool && !germanBool && !frenchBool && translation.getEnglishSense()!= null)
	    	    ){
	    	    	translations.add(translation);
	    	    }
	    	}
	    	
    	}catch(IOException ex){
    		Log.e("ResultLoader","IO Exception:  " + ex.toString());
    		return null;
    	}catch(Exception ex){
    		Log.e("ResultLoader","Exception: " + ex.toString());
    		return null;
    	}

    	mLastPart = mPart;
		mLastSearched = mExpression;
		mLastTranslations = translations.isEmpty()?null:translations;
    	
		return translations.isEmpty()?null:translations;
	}
	
	
    /**
     * Handles a request to start the Loader.
     */
    @Override 
    protected void onStartLoading() {
        forceLoad();
    }
	
        
}
