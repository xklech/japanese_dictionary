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
	private JSONArray japanese_kebJSON;
	
	private JSONArray englishJSON;
	private JSONArray englishJSONSense;
	
	private JSONArray frenchJSON;
	private JSONArray frenchJSONSense;	
	
	private JSONArray dutchJSON;
	private JSONArray dutchJSONSense;	
	
	private JSONArray germanJSON;
	private JSONArray germanJSONSense;
	
	String[] notificationTimeLeft;
	
	private Context context;
	private int i = 0;
	private int perc = 0;
	private int percSave = 0;
	
	public static final int ENTRIES = 170000;
	
	public SaxDataHolder(File file,Context appContext,NotificationManager nM,
			Notification notif,RemoteViews rV) throws IOException,SAXException{
        
		if(file == null){
			Log.e("SaxDataHolder", "SaxDataHolder - dictionary directory is null");
			throw new IllegalArgumentException("SaxParser: dictiona< ry directory is null");
		}
		context = appContext;
		Directory dir = FSDirectory.open(file);
		//Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_33);
    	Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,analyzer);
		mWriter = new IndexWriter(dir, config);
		
		mStartTime = System.currentTimeMillis();
		
		notificationTimeLeft = context.getString(R.string.dictionary_parsing_in_progress_time_left).split("t_l");
		if(notificationTimeLeft.length != 2 ){
			mNotificationView.setTextViewText(R.id.notification_text, context.getString(R.string.dictionary_parsing_in_progress_time_left));
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
			englishJSONSense = new JSONArray();
			frenchJSONSense = new JSONArray();
			dutchJSONSense = new JSONArray();
			germanJSONSense = new JSONArray();
			mJapaneseRebJSON = new JSONArray();
			japanese_kebJSON = new JSONArray();
		}else if("reb".equals(qName)){
			mJapaneseReb = true;
		}else if("keb".equals(qName)){
			mJapaneseKeb = true;
		}else if("sense".equals(qName)){
			
			englishJSON = new JSONArray();
			frenchJSON = new JSONArray();
			dutchJSON = new JSONArray();
			germanJSON = new JSONArray();
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
				japanese_kebJSON.put(new String(ch,start,length));	
				mJapaneseKeb = false;
			}
			if(mJapaneseReb){
				mJapaneseRebJSON.put(new String(ch,start,length));		
				mJapaneseReb = false;
			}
		}else if(mEnglish){
			englishJSON.put(new String(ch,start,length));
			//doc.add(new Field("english",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			mEnglish = false;			
		}else if(mFrench){
			frenchJSON.put(new String(ch,start,length));
			//doc.add(new Field("french",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			mFrench = false;			
		}else if(mDutch){
			dutchJSON.put(new String(ch,start,length));
			//doc.add(new Field("dutch",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			mDutch = false;			
		}else if(mGerman){
			germanJSON.put(new String(ch,start,length));
			//doc.add(new Field("german",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			mGerman = false;			
		}
		
	}
	
	
	
	public void endElement(String uri, String localName, 
	        String qName) throws SAXException { 
			if("sense".equals(qName)){
				if(englishJSON.length()>0){
					englishJSONSense.put(englishJSON);
				}
				if(frenchJSON.length()>0){
					frenchJSONSense.put(frenchJSON);
				}
				if(dutchJSON.length()>0){
					dutchJSONSense.put(dutchJSON);
				}
				if(germanJSON.length()>0){
					germanJSONSense.put(germanJSON);
				}
			}else if("entry".equals(qName)){
				if(japanese_kebJSON.length()>0){
					mDocument.add(new Field("japanese_keb",japanese_kebJSON.toString(),Field.Store.YES,Index.NO));
				}
				if(mJapaneseRebJSON.length()>0){
					mDocument.add(new Field("japanese_reb",mJapaneseRebJSON.toString(),Field.Store.YES,Index.NO));
				}
				if(englishJSONSense.length()>0){
					mDocument.add(new Field("english",englishJSONSense.toString(),Field.Store.YES,Index.NO));
					englishJSONSense = null;
				}
				if(frenchJSONSense.length()>0){
					mDocument.add(new Field("french",frenchJSONSense.toString(),Field.Store.YES,Index.NO));	
					frenchJSONSense = null;
				}
				if(dutchJSONSense.length()>0){
					mDocument.add(new Field("dutch",dutchJSONSense.toString(),Field.Store.YES,Index.NO));	
					dutchJSONSense = null;
				}
				if(germanJSONSense.length()>0){
					mDocument.add(new Field("german",germanJSONSense.toString(),Field.Store.YES,Index.NO));	
					germanJSONSense = null;
				}				
				
				try {
					i++;
					//System.out.println(i);
					mWriter.addDocument(mDocument);
					//System.out.println(doc.toString());
					int persPub = Math.round((((float)i/ENTRIES)*100)) ;
					
	                if(perc < persPub){
	                	if(percSave + 4 < persPub){
	                		mWriter.commit();
	                		System.out.println(persPub);
	                		percSave = persPub;
	                	}
	                	
	                	
	                	long duration  = System.currentTimeMillis() - mStartTime;
	                	mStartTime = System.currentTimeMillis();
	                	duration = duration * (100-persPub);
	                	
	                	mNotificationView.setProgressBar(R.id.ntification_progressBar, 100, persPub, false);
	                	if(notificationTimeLeft.length ==2){
	                		int timeLeft = Math.round(duration/60000);
	                		mNotificationView.setTextViewText(R.id.notification_text, notificationTimeLeft[0] + (timeLeft < 1?"<1":timeLeft) + notificationTimeLeft[1] );
	                	}
	                	mNotification.contentView = mNotificationView;
		                mNotifyManager.notify(0, mNotification);
	                	
		                perc = persPub;
		                Log.i("SaxDataHolder", "SaxDataHolder progress saved - " + perc + " %");
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
		LocalBroadcastManager.getInstance(context).registerReceiver(
				mReceiverDone, new IntentFilter("serviceCanceled"));
	}
	
	public void endDocument(){ 
		Log.i("SaxDataHolder", "End of document");
		LocalBroadcastManager.getInstance(context).unregisterReceiver(
				mReceiverDone);
			try {
				mWriter.close();
			} catch (IOException e) {
				Log.e("SaxDataHolder", "End of document - closinf lucene writer failed");
			}
    } 
	

	
}
