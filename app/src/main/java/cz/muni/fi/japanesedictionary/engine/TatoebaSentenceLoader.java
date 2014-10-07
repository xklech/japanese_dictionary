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
import java.util.List;

import cz.muni.fi.japanesedictionary.Const;
import cz.muni.fi.japanesedictionary.entity.TatoebaLangEnum;
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
            if(LOG) Log.w(LOG_TAG, "No path to tatoeba indices");
            return null;
        }
        File fileIndices = new File(pathToIndices);
        if(!fileIndices.exists() || !fileIndices.canRead()){
            if(LOG) Log.e(LOG_TAG, "Can't read tatoeba indices directory");
            return null;
        }

        String pathToSentences = settings.getString(Const.PREF_TATOEBA_SENTENCES_PATH, null);
        if(pathToSentences == null){
            if(LOG) Log.w(LOG_TAG, "No path to tatoeba sentences");
            return null;
        }
        File fileSentences = new File(pathToSentences);
        if(!fileSentences.exists() || !fileSentences.canRead()){
            if(LOG) Log.e(LOG_TAG, "Can't read tatoeba sentences directory");
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
            Directory indicesDir = FSDirectory.open(fileIndices);
            IndexReader indicesReader = IndexReader.open(indicesDir);
            IndexSearcher indicesSearcher= new IndexSearcher(indicesReader);
            TopScoreDocCollector indicesCollector = TopScoreDocCollector.create(5, false);
            indicesSearcher.search(q, indicesCollector);
            ScoreDoc[] hitsIndices = indicesCollector.topDocs().scoreDocs;

            QueryParser sentenceQueryParser = new QueryParser(Version.LUCENE_36, "japanese_id", analyzer);
            sentenceQueryParser.setPhraseSlop(0);

            Directory sentenceDir = FSDirectory.open(fileSentences);
            IndexReader sentenceReader = IndexReader.open(sentenceDir);
            IndexSearcher sentenceSearcher= new IndexSearcher(sentenceReader);




            List<TatoebaSentence> result = new ArrayList<>();
            for(ScoreDoc document : hitsIndices){
                int docId = document.doc;

                TatoebaSentence tatoebaSentence = new TatoebaSentence();

                Document d = indicesSearcher.doc(docId);
                String sentenceId = '"' + d.get("japanese_sentence_id") + '"';
                Log.d(LOG_TAG, "sentence id: " + sentenceId);
                TopScoreDocCollector sentenceCollector = TopScoreDocCollector.create(10, false);
                Query sentenceQuery = sentenceQueryParser.parse(sentenceId);
                sentenceSearcher.search(sentenceQuery, sentenceCollector);
                ScoreDoc[] hitsSentences = sentenceCollector.topDocs().scoreDocs;
                for(ScoreDoc sentenceScore : hitsSentences){
                    int sentenceDocId = sentenceScore.doc;
                    Document sentenceDocument = sentenceSearcher.doc(sentenceDocId);
                    String language  = sentenceDocument.get("language");
                    String sentence  = sentenceDocument.get("sentence");
                    Log.d(LOG_TAG, "language: " + language + ", sentence: " + sentence);
                    switch(language){
                        case TatoebaLangEnum.ENGLISH_CODE:
                            tatoebaSentence.setEnglish(sentence);
                            break;
                        case TatoebaLangEnum.DEUTSCH_CODE:
                            tatoebaSentence.setGerman(sentence);
                            break;
                        case TatoebaLangEnum.DUTCH_CODE:
                            tatoebaSentence.setDutch(sentence);
                            break;
                        case TatoebaLangEnum.FRENCH_CODE:
                            tatoebaSentence.setFrench(sentence);
                            break;
                        case TatoebaLangEnum.RUSSIAN_CODE:
                            tatoebaSentence.setRussian(sentence);
                            break;
                        case TatoebaLangEnum.JAPANESE_CODE:
                            tatoebaSentence.setJapaneseSentence(sentence);
                            break;
                    }
                }

                Log.d(LOG_TAG, "sentence: " + tatoebaSentence);
                result.add(tatoebaSentence);
            }
            return result.size() > 0 ? result : null;

        }catch(ParseException ex){
            Log.e(LOG_TAG,"Searching for sentences ParseException caught: " + ex);
        }catch(IOException ex){
            Log.e(LOG_TAG,"Searching for sentences IOException caught: "+ex);
        }catch(Exception ex){
            Log.e(LOG_TAG,"Searching for sentences Exception caught: "+ex);
        }




        return null;
    }


    @Override
    protected void onPostExecute(List<TatoebaSentence> tatoebaSentences) {
        mCallback.receiveSentences(tatoebaSentences);
    }
}
