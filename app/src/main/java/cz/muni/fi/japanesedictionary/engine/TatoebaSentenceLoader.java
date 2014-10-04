package cz.muni.fi.japanesedictionary.engine;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.ParseException;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.muni.fi.japanesedictionary.Const;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.fi.japanesedictionary.entity.TatoebaSentence;
import cz.muni.fi.japanesedictionary.interfaces.TatoebaSentenceCallback;
import cz.muni.fi.japanesedictionary.parser.ParserService;

/**
 * TatoebaSentenceLoader
 */
public class TatoebaSentenceLoader extends AsyncTask<List<String>, Void, List<TatoebaSentence>> {

    private static final String LOG_TAG = "TatoebaSentenceLoader";
    private static final boolean LOG = true;
    private Context mContext;
    private TatoebaSentenceCallback mCallback;

    public TatoebaSentenceLoader(Context context, TatoebaSentenceCallback callback){
        if(context == null){
            throw new IllegalArgumentException("context");
        }
        if(callback == null){
            throw new IllegalArgumentException("callback");
        }
        mContext = context;
        mCallback = callback;
    }


    @SafeVarargs
    @Override
    protected final List<TatoebaSentence> doInBackground(List<String>... params) {
        if( params == null || params.length != 1 || params[0] == null){
            throw new IllegalArgumentException("params");
        }
        List<String> words = params[0];
        if(words.isEmpty()){
            return null;
        }

        SharedPreferences settings = mContext.getSharedPreferences(ParserService.DICTIONARY_PREFERENCES, 0);
        String pathToIndices = settings.getString(Const.PREF_TATOEBA_INDICES_PATH, null);
        if(pathToIndices == null){
            if(LOG) Log.w(LOG_TAG, "No path to kanjidict2 dictionary");
            return null;
        }
        File file = new File(pathToIndices);
        if(!file.exists() || !file.canRead()){
            if(LOG) Log.e(LOG_TAG, "Can't read dictionary directory");
            return null;
        }

        StringBuilder queryBuilder = new StringBuilder();
        for(String word : words){
            if(queryBuilder.length() > 0){
                queryBuilder.append(' ');
            }
            queryBuilder.append('"').append(word).append('"');
        }
        String search = queryBuilder.toString();
        if(search.length() == 0){
            return null;
        }
        Analyzer analyzer = new CJKAnalyzer(Version.LUCENE_36);
        try{
            QueryParser query = new QueryParser(Version.LUCENE_36, "japanese_tag", analyzer);
            query.setPhraseSlop(0);

            Query q = query.parse(search);
            Directory dir = FSDirectory.open(file);
            IndexReader reader = IndexReader.open(dir);
            IndexSearcher mSearcher= new IndexSearcher(reader);
            TopScoreDocCollector collector = TopScoreDocCollector.create(100, false);
            mSearcher.search(q, collector);
            ScoreDoc[] hitsIndices = collector.topDocs().scoreDocs;

            List<TatoebaSentence> result = new ArrayList<>();
            for(ScoreDoc document : hitsIndices){
                int docId = document.doc;
                Document d = mSearcher.doc(docId);
                //d.get();

            }
            return result.size() > 0 ? result : null;

        }catch(ParseException ex){
            Log.e(LOG_TAG,"Searching for charaters ParseException caught: "+ex);
        }catch(IOException ex){
            Log.e(LOG_TAG,"Searching for charaters IOException caught: "+ex);
        }catch(Exception ex){
            Log.e(LOG_TAG,"Searching for charaters Exception caught: "+ex);
        }




        return null;
    }

}
