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
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import cz.muni.fi.japanesedictionary.R;


public class SaxDataHolder extends DefaultHandler{
	
	NotificationManager mNotifyManager = null;
	Notification mNotification = null;
	RemoteViews mNotificationView = null;
	
	IndexWriter w;
	Document doc;
	boolean japanese_keb;
	boolean japanese_reb;
	boolean english;
	boolean french;
	boolean dutch;
	boolean german;
	long startTime;
	
	JSONArray japanese_rebJSON;
	JSONArray japanese_kebJSON;
	
	JSONArray englishJSON;
	JSONArray englishJSONSense;
	
	JSONArray frenchJSON;
	JSONArray frenchJSONSense;	
	
	JSONArray dutchJSON;
	JSONArray dutchJSONSense;	
	
	JSONArray germanJSON;
	JSONArray germanJSONSense;
	
	
	Context context;
	int i = 0;
	int perc = 0;
	int percSave = 0;
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
		w = new IndexWriter(dir, config);
		
		startTime = System.currentTimeMillis();
		
		mNotifyManager = nM;
		mNotification = notif;
		mNotificationView = rV;
		Log.i("SaxDataHolder", "SaxDataHolder created");
	}
	
	
	public void startElement(String uri, String localName,String qName, 
            Attributes attributes) throws SAXException {		
			if("entry".equals(qName)){
				//System.out.println("Entry "+ (++i));
				doc = new Document();
				englishJSONSense = new JSONArray();
				frenchJSONSense = new JSONArray();
				dutchJSONSense = new JSONArray();
				germanJSONSense = new JSONArray();
				japanese_rebJSON = new JSONArray();
				japanese_kebJSON = new JSONArray();
			}else if("reb".equals(qName)){
				japanese_reb = true;
			}else if("keb".equals(qName)){
				japanese_keb = true;
			}else if("sense".equals(qName)){
				
				englishJSON = new JSONArray();
				frenchJSON = new JSONArray();
				dutchJSON = new JSONArray();
				germanJSON = new JSONArray();
			}else if("gloss".equals(qName)){
				if("eng".equals(attributes.getValue("xml:lang"))){
					//english
					english = true;
				}else if("fre".equals(attributes.getValue("xml:lang"))){
					french = true;
				}else if("dut".equals(attributes.getValue("xml:lang"))){
					dutch = true;
				}else if("ger".equals(attributes.getValue("xml:lang"))){
					german = true;
				}
			}
			
    }
	
	public void characters(char ch[], int start, int length) throws SAXException {
		if(japanese_keb || japanese_reb){
			doc.add(new Field("japanese","lucenematch "+new String(ch,start,length)+" lucenematch",Field.Store.NO, Index.ANALYZED));
			//System.out.println(new String(ch,start,length));
			if(japanese_keb){
				japanese_kebJSON.put(new String(ch,start,length));				
			}
			if(japanese_reb){
				japanese_rebJSON.put(new String(ch,start,length));				
			}
			japanese_keb = false;
			japanese_reb = false;
		}else if(english){
			englishJSON.put(new String(ch,start,length));
			//doc.add(new Field("english",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			english = false;			
		}else if(french){
			frenchJSON.put(new String(ch,start,length));
			//doc.add(new Field("french",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			french = false;			
		}else if(dutch){
			dutchJSON.put(new String(ch,start,length));
			//doc.add(new Field("dutch",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			dutch = false;			
		}else if(german){
			germanJSON.put(new String(ch,start,length));
			//doc.add(new Field("german",new String(ch,start,length),Field.Store.YES,Field.Index.NO));
			german = false;			
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
					doc.add(new Field("japanese_keb",japanese_kebJSON.toString(),Field.Store.YES,Index.NO));
				}
				if(japanese_rebJSON.length()>0){
					doc.add(new Field("japanese_reb",japanese_rebJSON.toString(),Field.Store.YES,Index.NO));
				}
				if(englishJSONSense.length()>0){
					doc.add(new Field("english",englishJSONSense.toString(),Field.Store.YES,Index.NO));
					englishJSONSense = null;
				}
				if(frenchJSONSense.length()>0){
					doc.add(new Field("french",frenchJSONSense.toString(),Field.Store.YES,Index.NO));	
					frenchJSONSense = null;
				}
				if(dutchJSONSense.length()>0){
					doc.add(new Field("dutch",dutchJSONSense.toString(),Field.Store.YES,Index.NO));	
					dutchJSONSense = null;
				}
				if(germanJSONSense.length()>0){
					doc.add(new Field("german",germanJSONSense.toString(),Field.Store.YES,Index.NO));	
					germanJSONSense = null;
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
	                		System.out.println(persPub);
	                		percSave = persPub;
	                	}
	                	
	                	
	                	long duration  = System.currentTimeMillis() - startTime;
	                	startTime = System.currentTimeMillis();
	                	duration = duration * (100-persPub);
	                	
	                	mNotificationView.setProgressBar(R.id.ntification_progressBar, 100, persPub, false);
	                	mNotificationView.setTextViewText(R.id.notification_text, context.getString(R.string.dictionary_parsing_in_progress) + " Time left: "+Math.round(duration/60000)+" min.");
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
	        	doc = null;
	        }
	    } 
	
	public void startDocument(){
		Log.i("SaxDataHolder", "Start of document");
	}
	
	public void endDocument(){ 
		Log.i("SaxDataHolder", "End of document");
			try {
				w.close();
			} catch (IOException e) {
				Log.e("SaxDataHolder", "End of document - closinf lucene writer failed");
			}
    } 
	

	
}
