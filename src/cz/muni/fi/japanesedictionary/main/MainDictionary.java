package cz.muni.fi.japanesedictionary.main;


import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.TextFragment;
import cz.muni.fi.japanesedictionary.parser.ParserService;

public class MainDictionary extends FragmentActivity implements DictionaryLanguageList.OnTextSelectedListener{
	
	boolean validDictionary;
	String pathToDictionary;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	SharedPreferences settings = getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
    	validDictionary = settings.getBoolean("hasValidDictionary", false);
    	pathToDictionary = settings.getString("pathToDictionary", null);
    	SharedPreferences.Editor edit = settings.edit();
    	edit.clear();
    	edit.commit();

    	/*GlossaryReaderContract database = new GlossaryReaderContract(getApplicationContext());
		Translation trans = database.getTranslation("necooJaponsky");
		database.close();*/
    	if(validDictionary && pathToDictionary != null){
	    	Analyzer  analyzer = new StandardAnalyzer(Version.LUCENE_33);
	    	try{
	    		QueryParser query = new QueryParser(Version.LUCENE_33, "japanese", analyzer);
	    		query.setPhraseSlop(0);
	    		Query q = query.parse("どうじょう"); //  彫像
	    		String indexFile = getExternalCacheDir().getAbsolutePath() + File.separator + "dictionary";
		    	Directory dir = new SimpleFSDirectory(new File(indexFile));
		    	IndexReader reader = IndexReader.open(dir);
		    	IndexSearcher searcher = new IndexSearcher(reader);
		    	TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
		    	searcher.search(q, collector);
		    	ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    	
		    	System.out.println("Found " + hits.length + " hits.");
		    	for(int i=0;i<hits.length;++i) {
		    	    int docId = hits[i].doc;
		    	    Document d = searcher.doc(docId);
		    	    System.out.println((i + 1) + ". " + d.get("english"));
		    	}
		    	searcher.close();
		    	
	    	}catch(Exception ex){
	    		System.out.println("vyjimka hledani: " + ex.toString());
	    	}
	    	
	    	AlertDialog alertDialogDictionary = new AlertDialog.Builder(MainDictionary.this).create();
	    	
	    	alertDialogDictionary.setCanceledOnTouchOutside(false);
	    	alertDialogDictionary.show();
		}else{
			AlertDialog alertDialogDictionary = new AlertDialog.Builder(MainDictionary.this).create();
			alertDialogDictionary.setTitle("Do not have valid dictionary");
			alertDialogDictionary.setMessage("We are sorry, but we don't have valid dictionary. Restart the application and install dictionary.");
			alertDialogDictionary.setCanceledOnTouchOutside(false);
			alertDialogDictionary.show();
		}
    	
    	/*
        setContentView(R.layout.fragment_container);
        
        if(savedInstanceState != null){
        	return ;
        }
        DictionaryLanguageList listFragment = new DictionaryLanguageList();
        listFragment.setArguments(getIntent().getExtras());
        
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,listFragment).commit();
        */
    }
	
	
	public void onTextSelected(int index){
		/* 0 english
		 * 1 french
		 * 2 dutch
		 * 3 german
		 */
		index = (index < 0 || index > 3)?0:index;
		TextFragment textFragment = new TextFragment();
		
		Bundle args = new Bundle();
		args.putInt(TextFragment.ACTUAL_INDEX, index);
		
		textFragment.setArguments(args);
		
		//swap transactions
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
		transaction.replace(R.id.fragment_container, textFragment);
		transaction.addToBackStack(null);
		
		transaction.commit();
	}

}
