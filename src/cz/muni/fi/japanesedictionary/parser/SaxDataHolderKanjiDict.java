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
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cz.muni.fi.japanesedictionary.R;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


/**
 * Sax data holder for kanjidict2 xml.
 * 
 * @author Jaroslav Klech
 *
 */
public class SaxDataHolderKanjiDict extends DefaultHandler{
	
	private static final String LOG_TAG = "SaxDataHolderKanjiDict";
	
	private NotificationManager mNotifyManager = null;
    private NotificationCompat.Builder mBuilder = null;
	private BroadcastReceiver mReceiverInterrupted= new BroadcastReceiver() {
		  @Override public void onReceive(Context context, Intent intent) { 
			  //		  intent can contain anydata 
			  mCanceled = true;
		  } };
	
	private boolean mCanceled = false;
	private IndexWriter mWriter;
	private Document mDoc;
	private long mStartTime;
	
	private String[] mNotificationTimeLeft;
	
	private Context mContext;
	private int mCountDone = 0;
	private int mPerc = 0;
	private int mPercSave = 0;
	public static final int ENTRIES_COUNT = 13150; // curently 13108
	
	
	//parsing
	private boolean mLiteral;
	private boolean mRadicalClassic;
	private boolean mGrade;
	private boolean mStrokeCount;
	private boolean mDicRef;

	private boolean mQueryCodeSkip;
	private boolean mRMGroupJaOn;
	private boolean mRMGroupJaKun;
	private boolean mMeaningEnglish;
	private boolean mMeaningFrench;
	/*
	 *  dutch and german aren't in current kanjidict 2
	 */
	private boolean mMeaningDutch;
	private boolean mMeaningGerman;
	
	private boolean mNanori;
	
	private JSONObject mValueDicRef;
	private String mDicRefKey;
	private JSONArray mValueRmGroupJaOn;
	private JSONArray mValueRmGroupJaKun;
	private JSONArray mValueMeaningEnglish;
	private JSONArray mValueMeaningFrench;
	/*
	 *  dutch and german aren't in current kanjidict 2
	 */
	private JSONArray mValueMeaningDutch;
	private JSONArray mValueMeaningGerman;
	
	private JSONArray mValueNanori;
	
	
	/**
	 * SaxDataHolderKanjiDict constructor
	 * 
	 * @param file lucene dictionary for saving documents
	 * @param appContext environment context
	 * @param nM instance of notification manager
	 * @throws IOException
	 * @throws IllegalArgumentException if directory doesn't exist
	 */
	public SaxDataHolderKanjiDict(File file,Context appContext, NotificationManager nM,
			NotificationCompat.Builder builder) throws IOException,SAXException{
        
		if(file == null){
			Log.e(LOG_TAG, "SaxDataHolderKanjiDict - dictionary directory is null");
			throw new IllegalArgumentException("SaxParser: dictionary directory is null");
		}
		mContext = appContext;
		Directory dir = FSDirectory.open(file);
    	Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,analyzer);
		mWriter = new IndexWriter(dir, config);
		
		mStartTime = System.currentTimeMillis();
        mBuilder = builder;
        mNotifyManager = nM;
		
		mNotificationTimeLeft = mContext.getString(R.string.dictionary_parsing_in_progress_time_left).split("t_l");
		if(mNotificationTimeLeft.length != 2 ){
            mBuilder.setContentText(mContext.getString(R.string.dictionary_parsing_in_progress_time_left));
		}

		Log.i(LOG_TAG, "SaxDataHolderKanjiDict created");
	}
	
	
	@Override
	public void startDocument() throws SAXException {
		Log.i(LOG_TAG, "Start of document");
		LocalBroadcastManager.getInstance(mContext).registerReceiver(
				mReceiverInterrupted, new IntentFilter("serviceCanceled"));
		super.startDocument();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(mCanceled){
			throw new SAXException("SAX terminated due to ParserService end.");
		}
		if("character".equals(qName)){
			mDoc = new Document();
			mValueDicRef = new JSONObject();
			mValueRmGroupJaOn = new JSONArray();
			mValueRmGroupJaKun = new JSONArray();
			mValueMeaningEnglish = new JSONArray();
			mValueMeaningFrench = new JSONArray();
			/*
			 *  dutch and german aren't in current kanjidict 2
			 */
			mValueMeaningDutch = new JSONArray();
			mValueMeaningGerman = new JSONArray();
			
			mValueNanori = new JSONArray();
		}else if("literal".equals(qName)){
			mLiteral = true;
		}else if("rad_value".equals(qName) && "classical".equals(attributes.getValue("rad_type"))){
			mRadicalClassic = true;
		}else if("grade".equals(qName)){
			mGrade = true;
		}else if("stroke_count".equals(qName)){
			mStrokeCount = true;
		}else if("dic_ref".equals(qName)){
			mDicRef = true;
			mDicRefKey = attributes.getValue("dr_type");
		}else if("q_code".equals(qName) && "skip".equals(attributes.getValue("qc_type"))){
			mQueryCodeSkip = true;
		}else if("reading".equals(qName)){
			if("ja_on".equals(attributes.getValue("r_type"))){
				mRMGroupJaOn = true;
			}else if("ja_kun".equals(attributes.getValue("r_type"))){
				mRMGroupJaKun = true;
			}
		}else if("meaning".equals(qName)){
			if(attributes.getValue("m_lang") == null){
				//english
				mMeaningEnglish = true;
			}else if("fr".equals(attributes.getValue("m_lang"))){
				mMeaningFrench = true;
			}else if("du".equals(attributes.getValue("m_lang"))){
				mMeaningDutch = true;
			}else if("ge".equals(attributes.getValue("m_lang"))){
				mMeaningGerman = true;
			}
		}else if("nanori".equals(qName)){
			mNanori = true;
		}
		super.startElement(uri, localName, qName, attributes);
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(mLiteral){
			mDoc.add(new Field("literal",new String(ch,start,length),Field.Store.YES, Index.ANALYZED));
			mLiteral = false;
		}else if(mRadicalClassic){
				String value = tryParseNumber(new String(ch,start,length));
				if(value != null){
					mDoc.add(new Field("radicalClassic",value,Field.Store.YES,Index.NO));
				}
				mRadicalClassic = false;
		}else if(mGrade){
			String value = tryParseNumber(new String(ch,start,length));
			if(value != null){
				mDoc.add(new Field("grade",value,Field.Store.YES,Index.NO));
			}
			mGrade = false;
		}else if(mStrokeCount){
			String value = tryParseNumber(new String(ch,start,length));
			if(value != null){
				mDoc.add(new Field("strokeCount",value,Field.Store.YES,Index.NO));
			}
			mStrokeCount = false;
		}else if(mDicRef){
			if(mDicRefKey != null){
				try {
					mValueDicRef.put(mDicRefKey, new String(ch,start,length));
				} catch (JSONException e) {
					Log.w(LOG_TAG, "valueDicRef.put failed");
				}
				mDicRefKey = null;
				mDicRef = false;
			}
		}else if(mQueryCodeSkip){
			mDoc.add(new Field("queryCodeSkip",new String(ch,start,length),Field.Store.YES,Index.NO));
			mQueryCodeSkip = false;
		}else if(mRMGroupJaOn){
			mValueRmGroupJaOn.put(new String(ch,start,length));
			mRMGroupJaOn = false;
		}else if(mRMGroupJaKun){
			mValueRmGroupJaKun.put(new String(ch,start,length));
			mRMGroupJaKun = false;
		}else if(mNanori){
			mValueNanori.put(new String(ch,start,length));
			mNanori = false;
		}else if(mMeaningEnglish){
			mValueMeaningEnglish.put(new String(ch,start,length));
			mMeaningEnglish = false;
		}else if(mMeaningFrench){
			mValueMeaningFrench.put(new String(ch,start,length));
			mMeaningFrench = false;
		}else if(mMeaningDutch){
			mValueMeaningDutch.put(new String(ch,start,length));
			mMeaningDutch = false;
		}else if(mMeaningGerman){
			mValueMeaningGerman.put(new String(ch,start,length));
			mMeaningGerman = false;
		}
		super.characters(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if("character".equals(qName)){
			if(mValueDicRef.length() > 0){
				mDoc.add(new Field("dicRef",mValueDicRef.toString(),Field.Store.YES,Index.NO));
			}
			if(mValueRmGroupJaOn.length() > 0){
				mDoc.add(new Field("rmGroupJaOn",mValueRmGroupJaOn.toString(),Field.Store.YES,Index.NO));
			}
			if(mValueRmGroupJaKun.length() > 0){
				mDoc.add(new Field("rmGroupJaKun",mValueRmGroupJaKun.toString(),Field.Store.YES,Index.NO));
			}
			if(mValueMeaningEnglish.length() > 0){
				mDoc.add(new Field("meaningEnglish",mValueMeaningEnglish.toString(),Field.Store.YES,Index.NO));
			}
			if(mValueMeaningFrench.length() > 0){
				mDoc.add(new Field("meaningFrench",mValueMeaningFrench.toString(),Field.Store.YES,Index.NO));
			}
			/*
			 *  dutch and german aren't in current kanjidict 2
			 */
			if(mValueMeaningDutch.length() > 0){
				mDoc.add(new Field("meaningDutch",mValueMeaningDutch.toString(),Field.Store.YES,Index.NO));
			}
			if(mValueMeaningGerman.length() > 0){
				mDoc.add(new Field("meaningGerman",mValueMeaningGerman.toString(),Field.Store.YES,Index.NO));
			}
			if(mValueNanori.length() > 0){
				mDoc.add(new Field("nanori",mValueNanori.toString(),Field.Store.YES,Index.NO));
			}

			try {
				mCountDone++;
				mWriter.addDocument(mDoc);
				int persPub = Math.round((((float)mCountDone/ENTRIES_COUNT)*100)) ;
				
                if(mPerc < persPub){
                	if(mPercSave + 4 < persPub){
                		mWriter.commit();
                		Log.i(LOG_TAG, "SaxDataHolder progress saved - " + persPub + " %");
                		mPercSave = persPub;
                	}
                	
                	
                	long duration  = System.currentTimeMillis() - mStartTime;
                	mStartTime = System.currentTimeMillis();
                	duration = duration * (100-persPub);
                	
                    mBuilder.setProgress(100, persPub, false);
                	if(mNotificationTimeLeft.length ==2){
                		int timeLeft = Math.round(duration/60000);
                        mBuilder.setContentText(mNotificationTimeLeft[0] + (timeLeft < 1?"<1":timeLeft) + mNotificationTimeLeft[1]);
                	}
	                mNotifyManager.notify(0, mBuilder.build());
                	
	                mPerc = persPub;
	                
                }
			} catch (CorruptIndexException e) {
				Log.e(LOG_TAG, "Saving doc - Adding document to lucene indexer failed: "+e.toString());
			} catch (Exception e){
				Log.e(LOG_TAG, "Saving doc: Unknown exception: "+e.toString());
			}
			mDoc = null;
		}
		
		super.endElement(uri, localName, qName);
	}
	
	
	@Override
	public void endDocument(){ 
		Log.i(LOG_TAG, "End of document");
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
				mReceiverInterrupted);
			try {
				mWriter.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, "End of document - closinf lucene writer failed");
			}
    } 
	
	
	
	
	/**
	 * Verifies whether given stringis number.
	 * @param parse strimng to be parsed as number
	 * @return original string or null if it isn't number
	 */
	private String tryParseNumber(String parse){
		if(parse == null){
			return null;
		}
		try{
			int number;
			number = Integer.parseInt(parse);
			if(number != 0){
				return String.valueOf(number);
			}
		}catch(NumberFormatException ex){
			Log.w(LOG_TAG,"Parsing number - NumberFormatException: "+ parse);
		}catch(Exception ex){
			Log.w(LOG_TAG,"Parsing number failed: " +parse);
		}
		return null;
	}
}
