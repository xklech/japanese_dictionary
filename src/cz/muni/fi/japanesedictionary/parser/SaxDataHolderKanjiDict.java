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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

public class SaxDataHolderKanjiDict extends DefaultHandler{
	private NotificationManager mNotifyManager = null;
	private Notification mNotification = null;
	private RemoteViews mNotificationView = null;
	private BroadcastReceiver mReceiverInterrupted= new BroadcastReceiver() {
		  @Override public void onReceive(Context context, Intent intent) { 
			  //		  intent can contain anydata 
			  canceled = true;
		  } };
	
	private boolean canceled = false;
	private IndexWriter w;
	private Document doc;
	private long startTime;
	
	String[] notificationTimeLeft;
	
	private Context context;
	private int i = 0;
	private int perc = 0;
	private int percSave = 0;
	public static final int ENTRIES = 13150; // curently 13108
	
	
	//parsing
	private boolean literal;
	private boolean radicalClassic;
	private boolean grade;
	private boolean strokeCount;
	private boolean dicRef;

	private boolean queryCodeSkip;
	private boolean rmGroupJaOn;
	private boolean rmGroupJaKun;
	private boolean meaningEnglish;
	private boolean meaningFrench;
	/*
	 *  dutch and german aren't in current kanjidict 2
	 */
	private boolean meaningDutch;
	private boolean meaningGerman;
	
	private boolean nanori;
	
	private JSONObject valueDicRef;
	private String dicRefKey;
	private JSONArray valueRmGroupJaOn;
	private JSONArray valueRmGroupJaKun;
	private JSONArray valueMeaningEnglish;
	private JSONArray valueMeaningFrench;
	/*
	 *  dutch and german aren't in current kanjidict 2
	 */
	private JSONArray valueMeaningDutch;
	private JSONArray valueMeaningGerman;
	
	private JSONArray valueNanori;
	
	public SaxDataHolderKanjiDict(File file,Context appContext,NotificationManager nM,
			Notification notif,RemoteViews rV) throws IOException,SAXException{
        
		if(file == null){
			Log.e("SaxDataHolderKanjiDict", "SaxDataHolderKanjiDict - dictionary directory is null");
			throw new IllegalArgumentException("SaxParser: dictiona< ry directory is null");
		}
		context = appContext;
		Directory dir = FSDirectory.open(file);
    	Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,analyzer);
		w = new IndexWriter(dir, config);
		
		startTime = System.currentTimeMillis();
		
		notificationTimeLeft = context.getString(R.string.dictionary_parsing_in_progress_time_left).split("t_l");
		if(notificationTimeLeft.length != 2 ){
			mNotificationView.setTextViewText(R.id.notification_text, context.getString(R.string.dictionary_parsing_in_progress_time_left));
		}
		mNotifyManager = nM;
		mNotification = notif;
		mNotificationView = rV;
		Log.i("SaxDataHolderKanjiDict", "SaxDataHolderKanjiDict created");
	}
	
	
	@Override
	public void startDocument() throws SAXException {
		Log.i("SaxDataHolderKanjiDict", "Start of document");
		LocalBroadcastManager.getInstance(context).registerReceiver(
				mReceiverInterrupted, new IntentFilter("serviceCanceled"));
		super.startDocument();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(canceled){
			throw new SAXException("SAX terminated due to ParserService end.");
		}
		if("character".equals(qName)){
			doc = new Document();
			valueDicRef = new JSONObject();
			valueRmGroupJaOn = new JSONArray();
			valueRmGroupJaKun = new JSONArray();
			valueMeaningEnglish = new JSONArray();
			valueMeaningFrench = new JSONArray();
			/*
			 *  dutch and german aren't in current kanjidict 2
			 */
			valueMeaningDutch = new JSONArray();
			valueMeaningGerman = new JSONArray();
			
			valueNanori = new JSONArray();
		}else if("literal".equals(qName)){
			literal = true;
		}else if("rad_value".equals(qName) && "classical".equals(attributes.getValue("rad_type"))){
			radicalClassic = true;
		}else if("grade".equals(qName)){
			grade = true;
		}else if("stroke_count".equals(qName)){
			strokeCount = true;
		}else if("dic_ref".equals(qName)){
			dicRef = true;
			dicRefKey = attributes.getValue("dr_type");
		}else if("q_code".equals(qName) && "skip".equals(attributes.getValue("qc_type"))){
			queryCodeSkip = true;
		}else if("reading".equals(qName)){
			if("ja_on".equals(attributes.getValue("r_type"))){
				rmGroupJaOn = true;
			}else if("ja_kun".equals(attributes.getValue("r_type"))){
				rmGroupJaKun = true;
			}
		}else if("meaning".equals(qName)){
			if(attributes.getValue("m_lang") == null){
				//english
				meaningEnglish = true;
			}else if("fr".equals(attributes.getValue("m_lang"))){
				meaningFrench = true;
			}else if("du".equals(attributes.getValue("m_lang"))){
				meaningDutch = true;
			}else if("ge".equals(attributes.getValue("m_lang"))){
				meaningGerman = true;
			}
		}else if("nanori".equals(qName)){
			nanori = true;
		}
		super.startElement(uri, localName, qName, attributes);
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(literal){
			doc.add(new Field("literal",new String(ch,start,length),Field.Store.YES, Index.ANALYZED));
			literal = false;
		}else if(radicalClassic){
				String value = tryParseNumber(new String(ch,start,length));
				if(value != null){
					doc.add(new Field("radicalClassic",value,Field.Store.YES,Index.NO));
				}
				radicalClassic = false;
		}else if(grade){
			String value = tryParseNumber(new String(ch,start,length));
			if(value != null){
				doc.add(new Field("grade",value,Field.Store.YES,Index.NO));
			}
			grade = false;
		}else if(strokeCount){
			String value = tryParseNumber(new String(ch,start,length));
			if(value != null){
				doc.add(new Field("strokeCount",value,Field.Store.YES,Index.NO));
			}
			strokeCount = false;
		}else if(dicRef){
			if(dicRefKey != null){
				try {
					valueDicRef.put(dicRefKey, new String(ch,start,length));
				} catch (JSONException e) {
					Log.w("SaxDataHolderKanjiDict", "valueDicRef.put failed");
				}
				dicRefKey = null;
				dicRef = false;
			}
		}else if(queryCodeSkip){
			doc.add(new Field("queryCodeSkip",new String(ch,start,length),Field.Store.YES,Index.NO));
			queryCodeSkip = false;
		}else if(rmGroupJaOn){
			valueRmGroupJaOn.put(new String(ch,start,length));
			rmGroupJaOn = false;
		}else if(rmGroupJaKun){
			valueRmGroupJaKun.put(new String(ch,start,length));
			rmGroupJaKun = false;
		}else if(nanori){
			valueNanori.put(new String(ch,start,length));
			nanori = false;
		}else if(meaningEnglish){
			valueMeaningEnglish.put(new String(ch,start,length));
			meaningEnglish = false;
		}else if(meaningFrench){
			valueMeaningFrench.put(new String(ch,start,length));
			meaningFrench = false;
		}else if(meaningDutch){
			valueMeaningDutch.put(new String(ch,start,length));
			meaningDutch = false;
		}else if(meaningGerman){
			valueMeaningGerman.put(new String(ch,start,length));
			meaningGerman = false;
		}
		super.characters(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if("character".equals(qName)){
			if(valueDicRef.length() > 0){
				doc.add(new Field("dicRef",valueDicRef.toString(),Field.Store.YES,Index.NO));
			}
			if(valueRmGroupJaOn.length() > 0){
				doc.add(new Field("rmGroupJaOn",valueRmGroupJaOn.toString(),Field.Store.YES,Index.NO));
			}
			if(valueRmGroupJaKun.length() > 0){
				doc.add(new Field("rmGroupJaKun",valueRmGroupJaKun.toString(),Field.Store.YES,Index.NO));
			}
			if(valueMeaningEnglish.length() > 0){
				doc.add(new Field("meaningEnglish",valueMeaningEnglish.toString(),Field.Store.YES,Index.NO));
			}
			if(valueMeaningFrench.length() > 0){
				doc.add(new Field("meaningFrench",valueMeaningFrench.toString(),Field.Store.YES,Index.NO));
			}
			/*
			 *  dutch and german aren't in current kanjidict 2
			 */
			if(valueMeaningDutch.length() > 0){
				doc.add(new Field("meaningDutch",valueMeaningDutch.toString(),Field.Store.YES,Index.NO));
			}
			if(valueMeaningGerman.length() > 0){
				doc.add(new Field("meaningGerman",valueMeaningGerman.toString(),Field.Store.YES,Index.NO));
			}
			if(valueNanori.length() > 0){
				doc.add(new Field("nanori",valueNanori.toString(),Field.Store.YES,Index.NO));
			}

			try {
				i++;
				//System.out.println(i);
				w.addDocument(doc);
				//System.out.println(doc.toString());
				int persPub = Math.round((((float)i/ENTRIES)*100)) ;
				
                if(perc < persPub){
                	if(percSave + 4 < persPub){
                		w.commit();
                		Log.i("SaxDataHolderKanjiDict", "SaxDataHolder progress saved - " + persPub + " %");
                		percSave = persPub;
                	}
                	
                	
                	long duration  = System.currentTimeMillis() - startTime;
                	startTime = System.currentTimeMillis();
                	duration = duration * (100-persPub);
                	
                	mNotificationView.setProgressBar(R.id.ntification_progressBar, 100, persPub, false);
                	if(notificationTimeLeft.length ==2){
                		int timeLeft = Math.round(duration/60000);
                		mNotificationView.setTextViewText(R.id.notification_text, notificationTimeLeft[0] + (timeLeft < 1?"<1":timeLeft) + notificationTimeLeft[1] );
                	}
                	mNotification.contentView = mNotificationView;
	                mNotifyManager.notify(0, mNotification);
                	
	                perc = persPub;
	                
                }
			} catch (CorruptIndexException e) {
				Log.e("SaxDataHolderKanjiDict", "Saving doc - Adding document to lucene indexer failed: "+e.toString());
			} catch (IOException e) {
				Log.e("SaxDataHolderKanjiDict", "Saving doc - Adding document to lucene indexer or commit failed: "+e.toString());
			} catch (Exception e){
				Log.e("SaxDataHolderKanjiDict", "Saving doc: Unknown exception: "+e.toString());
			}
        	doc = null;
		}
		
		super.endElement(uri, localName, qName);
	}
	
	
	
	public void endDocument(){ 
		Log.i("SaxDataHolderKanjiDict", "End of document");
		LocalBroadcastManager.getInstance(context).unregisterReceiver(
				mReceiverInterrupted);
			try {
				w.close();
			} catch (IOException e) {
				Log.e("SaxDataHolderKanjiDict", "End of document - closinf lucene writer failed");
			}
    } 
	
	
	
	
	/**
	 * private functions
	 */
	
	private String tryParseNumber(String parse){
		if(parse == null){
			return null;
		}
		try{
			int number = 0;
			number = Integer.parseInt(parse);
			if(number != 0){
				return String.valueOf(number);
			}
		}catch(NumberFormatException ex){
			Log.w("SaxDataHolderKanjiDict","Parsing number - NumberFormatException: "+ parse);
		}catch(Exception ex){
			Log.w("SaxDataHolderKanjiDict","Parsinnumber failed: " +parse);
		}
		return null;
	}
}
