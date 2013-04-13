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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import cz.muni.fi.japanesedictionary.R;


public class SaxDataHolder extends DefaultHandler{
	
	private NotificationManager mNotifyManager = null;
	private Notification mNotification = null;
	private RemoteViews mNotificationView = null;
	
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
	
	private String[] mNotificationTimeLeft;
	
	private Context mContext;
	private int mCountDone = 0;
	private int mPerc = 0;
	private int mPercSave = 0;
	
	public static final int ENTRIES_COUNT = 170000;
	
	public SaxDataHolder(File file,Context appContext,NotificationManager nM,
			Notification notif,RemoteViews rV) throws IOException,SAXException{
        
		if(file == null){
			Log.e("SaxDataHolder", "SaxDataHolder - dictionary directory is null");
			throw new IllegalArgumentException("SaxParser: dictiona< ry directory is null");
		}
		mContext = appContext;
		Directory dir = FSDirectory.open(file);
		//Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_33);
    	Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,analyzer);
		mWriter = new IndexWriter(dir, config);
		
		mStartTime = System.currentTimeMillis();
		
		mNotificationTimeLeft = mContext.getString(R.string.dictionary_parsing_in_progress_time_left).split("t_l");
		if(mNotificationTimeLeft.length != 2 ){
			mNotificationView.setTextViewText(R.id.notification_text, mContext.getString(R.string.dictionary_parsing_in_progress_time_left));
		}
		mNotifyManager = nM;
		mNotification = notif;
		mNotificationView = rV;
		Log.i("SaxDataHolder", "SaxDataHolder created");
	}
	
	
	
	public void startElement(String uri, String localName,String qName, 
            Attributes attributes) throws SAXException {		
		if(mCanceled){
			throw new SAXException("SAX terminated due to ParserService end.");
		}
		if("entry".equals(qName)){
			//System.out.println("Entry "+ (++i));
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
		}
			
    }
	
	public void characters(char ch[], int start, int length) throws SAXException {
		if(mJapaneseKeb || mJapaneseReb){
			mDocument.add(new Field("japanese","lucenematch "+new String(ch,start,length)+" lucenematch",Field.Store.NO, Index.ANALYZED));
			//System.out.println(new String(ch,start,length));
			if(mJapaneseKeb){
				mJapaneseKebJSON.put(new String(ch,start,length));	
				mJapaneseKeb = false;
			}
			if(mJapaneseReb){
				mJapaneseRebJSON.put(new String(ch,start,length));		
				mJapaneseReb = false;
			}
		}else if(mEnglish){
			mEnglishJSON.put(new String(ch,start,length));
			//doc.add(new Field("english",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			mEnglish = false;			
		}else if(mFrench){
			mFrenchJSON.put(new String(ch,start,length));
			//doc.add(new Field("french",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			mFrench = false;			
		}else if(mDutch){
			mDutchJSON.put(new String(ch,start,length));
			//doc.add(new Field("dutch",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			mDutch = false;			
		}else if(mGerman){
			mGermanJSON.put(new String(ch,start,length));
			//doc.add(new Field("german",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			mGerman = false;			
		}
		
	}
	
	
	
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
				
				try {
					mCountDone++;
					//System.out.println(i);
					mWriter.addDocument(mDocument);
					//System.out.println(doc.toString());
					int persPub = Math.round((((float)mCountDone/ENTRIES_COUNT)*100)) ;
					
	                if(mPerc < persPub){
	                	if(mPercSave + 4 < persPub){
	                		mWriter.commit();
	                		System.out.println(persPub);
	                		mPercSave = persPub;
	                	}
	                	
	                	
	                	long duration  = System.currentTimeMillis() - mStartTime;
	                	mStartTime = System.currentTimeMillis();
	                	duration = duration * (100-persPub);
	                	
	                	mNotificationView.setProgressBar(R.id.ntification_progressBar, 100, persPub, false);
	                	if(mNotificationTimeLeft.length ==2){
	                		int timeLeft = Math.round(duration/60000);
	                		mNotificationView.setTextViewText(R.id.notification_text, mNotificationTimeLeft[0] + (timeLeft < 1?"<1":timeLeft) + mNotificationTimeLeft[1] );
	                	}
	                	mNotification.contentView = mNotificationView;
		                mNotifyManager.notify(0, mNotification);
	                	
		                mPerc = persPub;
		                Log.i("SaxDataHolder", "SaxDataHolder progress saved - " + mPerc + " %");
	                }
				} catch (CorruptIndexException e) {
					Log.e("SaxDataHolder", "Saving doc - Adding document to lucene indexer failed: "+e.toString());
				} catch (IOException e) {
					Log.e("SaxDataHolder", "Saving doc - Adding document to lucene indexer or commit failed: "+e.toString());
				} catch (Exception e){
					Log.e("SaxDataHolder", "Saving doc: Unknown exception: "+e.toString());
				}
				mDocument = null;
	        }
	    } 
	
	public void startDocument(){
		Log.i("SaxDataHolder", "Start of document");
		LocalBroadcastManager.getInstance(mContext).registerReceiver(
				mReceiverDone, new IntentFilter("serviceCanceled"));
	}
	
	public void endDocument(){ 
		Log.i("SaxDataHolder", "End of document");
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
				mReceiverDone);
			try {
				mWriter.close();
			} catch (IOException e) {
				Log.e("SaxDataHolder", "End of document - closinf lucene writer failed");
			}
    } 
	

	
}
