package cz.muni.fi.japanesedictionary.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.interfaces.SearchListener;
import cz.muni.fi.japanesedictionary.parser.ParserService;
import cz.muni.fi.japanesedictionary.parser.RomanizationEnum;

/**
 * Loader for ResultFragmentList. Searches JMdict for match with expression.
 * 
 * @author Jaroslav Klech
 *
 */
public class FragmentListAsyncTask extends AsyncTask<String, Translation, List<Translation>> {

	private Context mContext;
	final private SearchListener mSearchListener;
	/**
	 * Constructor for FragmentListAsyncTask
	 * 
	 * @param list List implementing SearchListener
	 * @param cont environment context
	 */
	public FragmentListAsyncTask(SearchListener list, Context context){
		mSearchListener = list;
		mContext = context;
	}
	
	/**
	 * Loads translation using Lucene 
	 */
	@Override
	protected List<Translation> doInBackground(String... params) {
		String expression = params[0];
		String part = params[1];
		
        SharedPreferences settings = mContext.getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
        String pathToDictionary = settings.getString("pathToDictionary", null);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final boolean englishBool = sharedPrefs.getBoolean("language_english", false);
        final boolean frenchBool = sharedPrefs.getBoolean("language_french", false);        
        final boolean dutchBool = sharedPrefs.getBoolean("language_dutch", false);
        final boolean germanBool = sharedPrefs.getBoolean("language_german", false);
        final List<Translation> translations = new ArrayList<Translation>();
        
        if(pathToDictionary == null){
        	Log.e("FragmentListAsyncTask", "No path to jmdict dictionary");
        	return null;
        }
        File file = new File(pathToDictionary);
        if(file == null || !file.exists() || !file.canRead()){
        	Log.e("FragmentListAsyncTask", "Cant read jmdict dictionary directory");
        	return null;
        }
        
        if(expression == null){
        	//first run 
        	Log.i("FragmentListAsyncTask","First run - last 10 translations ");
        	GlossaryReaderContract database = new GlossaryReaderContract(mContext);
        	List<Translation> translationsTemp = database.getLastTranslations(10);
        	database.close();
        	return translationsTemp;
        }
        
        if(expression.length() < 1){
        	Log.w("FragmentListAsyncTask", "No expression to translate");
        	return null;
        }
        Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
        IndexSearcher searcher = null;
    	try{
    		String search;    		
    		boolean onlyReb = false;
    		if(Pattern.matches("\\p{Latin}*", expression)){
    			//only romaji
    			onlyReb = true;
    			Log.i("FragmentListAsyncTask","Only letters, converting to hiragana. ");
    			expression = RomanizationEnum.Hepburn.toHiragana(expression);
    		}

    		
    		if("end".equals(part)){
    			search = "\""+expression + " lucenematch\"";
    		}else if("beginning".equals(part)){
    			search = "\"lucenematch " + expression+"\"";	
    		}else if("middle".equals(part)){
    			search = "\""+expression+"\"";
    		}else {
    			search = "\"lucenematch "+expression + " lucenematch\"";
    		}
    		Log.i("FragmentListAsyncTask"," Searching for: "+search);
    		
    		
    		Query q;
    		if(onlyReb){
    			Log.e("FragmentListAsyncTask","jednoduchej dotaz");
    			q = (new QueryParser(Version.LUCENE_36, "index_japanese_reb", analyzer)).parse(search);
    		}else{
        		q= MultiFieldQueryParser.parse(Version.LUCENE_36, new String[] {search,search},   new String[] {"index_japanese_keb","index_japanese_reb"},analyzer);
    		}

    		
 	    	Directory dir = FSDirectory.open(file);
	    	IndexReader reader = IndexReader.open(dir);
	    	searcher= new IndexSearcher(reader);
	    	
	    	Collector collector = new Collector(){
	    		int max = 1000;
	    		int count = 0;
	    		IndexReader reader;
	    		
				@Override
				public boolean acceptsDocsOutOfOrder() {
					return true;
				}

				@Override
				public void collect(int docID) throws IOException {
					
					Document d = reader.document(docID);
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
		    	    	
		    	    	count++;
		    	    	if(count < max){
		    	    		if(!FragmentListAsyncTask.this.isCancelled()){
		    	    			FragmentListAsyncTask.this.publishProgress(translation);
			    	    		translations.add(translation);
		    	    		}else{
		    	    			translations.clear();
			    	    		throw new IOException("Loader canceled");
		    	    		}
		    	    	}else{
		    	    		throw new IOException("Max exceeded");
		    	    	}
		    	    }
				}

				@Override
				public void setNextReader(IndexReader _reader, int base)
						throws IOException {
					reader = _reader;
				}

				@Override
				public void setScorer(Scorer arg0) throws IOException {					
				}
	    		
	    	};

	    	
	    	searcher.search(q, collector);
	    	searcher.close();
    	}catch(IOException ex){
    		Log.e("FragmentListAsyncTask","IO Exception:  " + ex.toString());
    		try {
    			if(searcher != null){
    				searcher.close();
    			}
			} catch (IOException e) {
				e.printStackTrace();
			}
    		return translations;
    	}catch(Exception ex){
    		Log.e("FragmentListAsyncTask","Exception: " + ex.toString());
    		return null;
    	}
    	
		return translations.isEmpty()?null:translations;
	}
	
	/**
	 * Adding individual translations to list fragment
	 */
	@Override
	protected void onProgressUpdate(Translation... values) {
		if(!isCancelled()){
			mSearchListener.onResultFound(values[0]);
		}
		super.onProgressUpdate(values);
	}
	
	/**
	 * Sets list of translations to list fragment
	 */
	@Override
	protected void onPostExecute(List<Translation> result) {
		if(!isCancelled()){
			mSearchListener.onLoadFinished(result);
		}

		super.onPostExecute(result);
	}

	
	
	
}
