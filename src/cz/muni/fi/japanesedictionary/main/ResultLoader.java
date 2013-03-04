package cz.muni.fi.japanesedictionary.main;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import cz.muni.fi.japanesedictionary.parser.ParserService;

public class ResultLoader extends AsyncTaskLoader<List<Translation>>{
	private Context context;
	private String expression;
	private String part;
	private IndexSearcher searcher;

	private Handler handler = new ResultLoaderHandler(this);
	
	public ResultLoader(Context cont,String expr,String _part) {
		super(cont);
		expression = expr;
		context = cont;
		part = _part;
		Log.e("ResultLoader", "part constructor: "+part);
		
	}
	
	public void setExpression(String exp){
		Log.i("ResultLoader","set expression");
		expression = exp;
		this.onContentChanged();
	}
	
	public void setWordPart(String _part){
		Log.i("ResultLoader","set word part");
		part = _part;
		this.onContentChanged();
	}
	
	public Handler getHandler(){
		return handler;
	}
	
	public String getExpression(){
		return expression;
	}

	@Override
	public List<Translation> loadInBackground() {
		
        SharedPreferences settings = context.getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
        boolean validDictionary = settings.getBoolean("hasValidDictionary", false);
        String pathToDictionary = settings.getString("pathToDictionary", null);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean englishBool = sharedPrefs.getBoolean("language_english", false);
        boolean frenchBool = sharedPrefs.getBoolean("language_french", false);        
        boolean dutchBool = sharedPrefs.getBoolean("language_dutch", false);
        boolean germanBool = sharedPrefs.getBoolean("language_german", false);
        List<Translation> translations = new ArrayList<Translation>();
        
        if(!validDictionary){
        	Log.e("ResultLoader", "No dictionary");
        	return null;
        }
        if(pathToDictionary == null){
        	Log.e("ResultLoader", "No path to dictionary");
        	return null;
        }
        File file = new File(pathToDictionary);
        if(file == null || !file.canRead()){
        	Log.e("ResultLoader", "Cant read dictionary directory");
        	return null;
        }
        if(expression == null || expression.length() == 0){
        	Log.i("ResultLoader", "No expression to translate");
        	return null;
        }
        Log.e("fragment", "maam");
        
        
		
    	Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
    	try{
    		QueryParser query = new QueryParser(Version.LUCENE_36, "japanese", analyzer);
    		query.setPhraseSlop(0);
    		String search;
    		Log.e("ResultLoader", "part search: "+part);
    		if("end".equals(part)){
    			Log.i("ResultLoader","end");
    			search = "\""+expression + " lucenematch\"";
    		}else if("begining".equals(part)){
    			Log.i("ResultLoader","begining");
    			search = "\"lucenematch " + expression+"\"";	
    		}else if("middle".equals(part)){
    			Log.i("ResultLoader","middle");
    			search = expression;
    		}else {
    			Log.i("ResultLoader","exactly same");
    			search = "\"lucenematch "+expression + " lucenematch\"";
    		}
    		Log.i("ResultLoader", "Searching for: "+search);
    		Query q = query.parse(search); 
	    	 if( searcher == null){
	 	    	Directory dir = FSDirectory.open(file);
		    	IndexReader reader = IndexReader.open(dir);
	    		searcher= new IndexSearcher(reader);
	    	 }
	    	TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
	    	searcher.search(q, collector);
	    	ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    	Log.e("ResultLoader", String.valueOf(hits.length));
	    	for(int i=0;i<hits.length;++i) {
	    	    int docId = hits[i].doc;
	    	    Document d = searcher.doc(docId);
	    	    Translation translation = new Translation();
	    	    //Log.e("df", d.toString());
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
	    	
    	}catch(Exception ex){
    		Log.e("ResultLoader","vyjimka hledani: " + ex.toString());
    	}

		return translations.isEmpty()?null:translations;
	}
	
	
    /**
     * Handles a request to start the Loader.
     */
    @Override 
    protected void onStartLoading() {
        forceLoad();
    }
	
    
    static class ResultLoaderHandler extends Handler {
        private final WeakReference<ResultLoader> mLoader; 

        ResultLoaderHandler(ResultLoader loader) {
        	mLoader = new WeakReference<ResultLoader>(loader);
        }
        @Override
        public void handleMessage(Message msg)
        {
        	ResultLoader loader = mLoader.get();
             if (loader != null) { 
            	 Bundle bundle = msg.getData();
            	 String translation = bundle.getString(MainActivity.HANDLER_BUNDLE_TRANSLATION);
            	 if(translation != null){
            		 loader.setExpression(translation);
            		 return;
                 }
            	 String part = bundle.getString(MainActivity.HANDLER_BUNDLE_TAB);
            	 if(part != null){
            		 loader.setWordPart(part);
            		 return;
                 }
             }
        }
    }
        
}
