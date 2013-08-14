/**
 *     JapaneseDictionary - an JMDict browser for Android
 Copyright (C) 2013 Jaroslav Klech
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.muni.fi.japanesedictionary.parser;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import cz.muni.fi.japanesedictionary.R;

/**
 * Sax data holder for JMdict xml.
 * 
 * @author Jaroslav Klech
 *
 */
public class SaxDataHolder extends DefaultHandler{
	
	private static final String LOG_TAG = "SaxDataHolder";
	
	private NotificationManager mNotifyManager = null;
    private NotificationCompat.Builder mBuilder = null;

	private BroadcastReceiver mReceiverDone= new BroadcastReceiver() {
		  @Override 
		  public void onReceive(Context context, Intent intent) { 
			  //		  intent can contain anydata 
			  mCanceled = true;
		  } };
	
	private boolean mCanceled = false;
		  
	private IndexWriter mWriter;
	private Document mDocument;
	private boolean mJapaneseKeb;
	private boolean mJapaneseReb;
	private boolean mEnglish;
	private boolean mFrench;
	private boolean mDutch;
	private boolean mGerman;
    private boolean mPriorityTag;
	private long mStartTime;
	
	private JSONArray mJapaneseRebJSON;
	private JSONArray mJapaneseKebJSON;
	
	private JSONArray mEnglishJSON;
	private JSONArray mEnglishJSONSense;
	
	private JSONArray mFrenchJSON;
	private JSONArray mFrenchJSONSense;	
	
	private JSONArray mDutchJSON;
	private JSONArray mDutchJSONSense;	
	
	private JSONArray mGermanJSON;
	private JSONArray mGermanJSONSense;

    private boolean mPrioritized;
	
	private Context mContext;
	private int mCountDone = 0;
	private int mPerc = 0;
	private int mPercSave = 0;
	
	
	public static final int ENTRIES_COUNT = 170000;
	
	/**
	 * SaxDataHolder constructor
	 * 
	 * @param file lucene dictionary for saving documents
	 * @param appContext environment context
	 * @param nM instance of notification manager
	 * @throws IOException
	 * @throws IllegalArgumentException if directory doesn't exist
	 */
	public SaxDataHolder(File file,Context appContext,NotificationManager nM,
                         NotificationCompat.Builder builder) throws IOException,IllegalArgumentException{
        
		if(file == null){
			Log.e(LOG_TAG, "SaxDataHolder - dictionary directory is null");
			throw new IllegalArgumentException("SaxParser: dictionary directory is null");
		}
		mContext = appContext;
		Directory dir = FSDirectory.open(file);
		//Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_33);
    	Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,analyzer);
		mWriter = new IndexWriter(dir, config);
		
		mStartTime = System.currentTimeMillis();

        mBuilder = builder;
        mNotifyManager = nM;

		Log.i(LOG_TAG, "SaxDataHolder created");
	}
	
	

	@Override
	public void startElement(String uri, String localName,String qName, 
            Attributes attributes) throws SAXException {		
		if(mCanceled){
			throw new SAXException("SAX terminated due to ParserService end.");
		}
		if("entry".equals(qName)){
			mDocument = new Document();
			mEnglishJSONSense = new JSONArray();
			mFrenchJSONSense = new JSONArray();
			mDutchJSONSense = new JSONArray();
			mGermanJSONSense = new JSONArray();
			mJapaneseRebJSON = new JSONArray();
			mJapaneseKebJSON = new JSONArray();
		}else if("reb".equals(qName)){
			mJapaneseReb = true;
		}else if("keb".equals(qName)){
			mJapaneseKeb = true;
		}else if("sense".equals(qName)){
			
			mEnglishJSON = new JSONArray();
			mFrenchJSON = new JSONArray();
			mDutchJSON = new JSONArray();
			mGermanJSON = new JSONArray();
		}else if("gloss".equals(qName)){
			if("eng".equals(attributes.getValue("xml:lang"))){
				//english
				mEnglish = true;
			}else if("fre".equals(attributes.getValue("xml:lang"))){
				mFrench = true;
			}else if("dut".equals(attributes.getValue("xml:lang"))){
				mDutch = true;
			}else if("ger".equals(attributes.getValue("xml:lang"))){
				mGerman = true;
			}
		}else if("ke_pri".equals(qName) || "re_pri".equals(qName)){
            mPriorityTag = true;
        }
			
    }
	
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if(mJapaneseKeb || mJapaneseReb){
			String japString = new String(ch,start,length);
			//gives space after letters: pepa => p e p a
			String indexString = japString.replaceAll(".(?!$)", "$0 ");
			mDocument.add(new Field("japanese","lucenematch "+indexString+"lucenematch",Field.Store.NO, Index.ANALYZED));
			if(mJapaneseKeb){
				mJapaneseKebJSON.put(japString);	
				mJapaneseKeb = false;
			}
			if(mJapaneseReb){
				mDocument.add(new Field("index_japanese_reb","lucenematch "+indexString+"lucenematch",Field.Store.NO, Index.ANALYZED));
				mJapaneseRebJSON.put(japString);		
				mJapaneseReb = false;
			}
		}else if(mEnglish){
			mEnglishJSON.put(new String(ch,start,length));
			mEnglish = false;			
		}else if(mFrench){
			mFrenchJSON.put(new String(ch,start,length));
			mFrench = false;			
		}else if(mDutch){
			mDutchJSON.put(new String(ch,start,length));
			mDutch = false;			
		}else if(mGerman){
			mGermanJSON.put(new String(ch,start,length));
			mGerman = false;			
		}else if(mPriorityTag){
            String string = new String(ch,start,length);
            if("news1".equals(string) || "ichi1".equals(string) || "spec1".equals(string) || "gai1".equals(string)){
                mPrioritized = true;
            }
            mPriorityTag = false;
        }
		
	}
	
	
	@Override
	public void endElement(String uri, String localName, 
	        String qName) throws SAXException { 
			if("sense".equals(qName)){
				if(mEnglishJSON.length()>0){
					mEnglishJSONSense.put(mEnglishJSON);
				}
				if(mFrenchJSON.length()>0){
					mFrenchJSONSense.put(mFrenchJSON);
				}
				if(mDutchJSON.length()>0){
					mDutchJSONSense.put(mDutchJSON);
				}
				if(mGermanJSON.length()>0){
					mGermanJSONSense.put(mGermanJSON);
				}
			}else if("entry".equals(qName)){
				if(mJapaneseKebJSON.length()>0){
					mDocument.add(new Field("japanese_keb",mJapaneseKebJSON.toString(),Field.Store.YES,Index.NO));
				}
				if(mJapaneseRebJSON.length()>0){
					mDocument.add(new Field("japanese_reb",mJapaneseRebJSON.toString(),Field.Store.YES,Index.NO));
				}
				if(mEnglishJSONSense.length()>0){
					mDocument.add(new Field("english",mEnglishJSONSense.toString(),Field.Store.YES,Index.NO));
					mEnglishJSONSense = null;
				}
				if(mFrenchJSONSense.length()>0){
					mDocument.add(new Field("french",mFrenchJSONSense.toString(),Field.Store.YES,Index.NO));	
					mFrenchJSONSense = null;
				}
				if(mDutchJSONSense.length()>0){
					mDocument.add(new Field("dutch",mDutchJSONSense.toString(),Field.Store.YES,Index.NO));	
					mDutchJSONSense = null;
				}
				if(mGermanJSONSense.length()>0){
					mDocument.add(new Field("german",mGermanJSONSense.toString(),Field.Store.YES,Index.NO));	
					mGermanJSONSense = null;
				}				
				if(mPrioritized){
                    mPrioritized = false;
                    mDocument.add(new Field("prioritized", "true", Field.Store.YES,Index.NO));
                }
				try {
					mCountDone++;
					mWriter.addDocument(mDocument);
					int persPub = Math.round((((float)mCountDone/ENTRIES_COUNT)*100)) ;
					
	                if(mPerc < persPub){
	                	if(mPercSave + 4 < persPub){
	                		mWriter.commit();
	                		Log.i(LOG_TAG, "Save: "+ mPercSave+" %");
	                		mPercSave = persPub;
	                	}
	                	
	                	
	                	long duration  = System.currentTimeMillis() - mStartTime;
	                	mStartTime = System.currentTimeMillis();
	                	duration = duration * (100-persPub);
                        int timeLeft = Math.round(duration/60000);

                        mBuilder.setProgress(100, persPub, false)
                                .setContentText(mContext.getResources().getQuantityString(R.plurals.dictionary_parsing_in_progress_time_left, timeLeft, timeLeft))
                                .setContentInfo(persPub + "%");

		                mNotifyManager.notify(0, mBuilder.build());
	                	
		                mPerc = persPub;
		                Log.i(LOG_TAG, "SaxDataHolder progress saved - " + mPerc + " %");
	                }
				} catch (CorruptIndexException e) {
					Log.e(LOG_TAG, "Saving doc - Adding document to lucene indexer failed: "+e.toString());
				} catch (Exception e){
					Log.e(LOG_TAG, "Saving doc: Unknown exception: "+e.toString());
				}
				mDocument = null;
	        }
	    } 
	
	@Override
	public void startDocument(){
		Log.i(LOG_TAG, "Start of document");
		LocalBroadcastManager.getInstance(mContext).registerReceiver(
				mReceiverDone, new IntentFilter("serviceCanceled"));
	}
	
	@Override
	public void endDocument(){ 
		Log.i(LOG_TAG, "End of document");
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
				mReceiverDone);
			try {
				mWriter.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, "End of document - closinf lucene writer failed");
			}
    } 
	

	
}
