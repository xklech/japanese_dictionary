package cz.muni.fi.japanesedictionary.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.main.MainActivity;



public class ParserService extends IntentService{
	
	public static final String DICTIONARY_PATH = "http://index.aerolines.cz/JMdict.gz";
	public static final String KANJIDICT_PATH = "http://www.csse.monash.edu.au/~jwb/kanjidic2/kanjidic2.xml.gz";
	public static final String DICTIONARY_PREFERENCES = "cz.muni.fi.japanesedictionary";
	
	InputStream input = null;
	OutputStream output = null;
	NotificationManager mNotifyManager = null;
	Notification mNotification = null;
	RemoteViews mNotificationView = null;
	boolean canceled = false;
	boolean complete = false;

	public ParserService(){
		super("ParserService");
	}

	@Override
	public void onCreate(){
		super.onCreate();
		Log.i("ParserService", "Creating parser service");
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		mNotificationView = new RemoteViews(this.getPackageName(),R.layout.notification); 
		mNotificationView.setImageViewResource(R.id.notification_image, R.drawable.ic_launcher);
		mNotificationView.setTextViewText(R.id.notification_title, getString(R.string.dictionary_download_title));
		mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_in_progress) + " 0 %");
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setAutoCancel(true);
		mBuilder.setSmallIcon(R.drawable.ic_launcher);

		Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
		resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent resultPendingIntent =
				PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		mNotification = mBuilder.build();
		mNotification.icon = R.drawable.ic_launcher;
		mNotification.contentView=mNotificationView;
		mNotifyManager.notify(0, mNotification);
	
	}
	
	
	@Override
	protected void onHandleIntent(Intent arg0) {
		boolean downloadedJapDict = false;
		boolean downloadedKanjiDict = false;
		String dictionaryPath = null;
		String kanjiDictPath = null;
		
		URL url = null;
		try{
			url = new URL(ParserService.DICTIONARY_PATH);
		}catch(MalformedURLException ex){
			Log.e("ParserService", "Error: creating url for downloading dictionary");
			return;
		}
		
		URLConnection connection = null;
		long fileLength;
		
		
		try {
			connection = url.openConnection();
			connection.setRequestProperty("Accept-Encoding", "identity");
			connection.connect();
			//velikost souboru
			fileLength = connection.getContentLength();
			input = new BufferedInputStream(connection.getInputStream(),1024);
			
			File karta = null;
			if(MainActivity.canWriteExternalStorage()){
				//je dostupna karta
				karta = getExternalCacheDir();
			}
			if(karta == null){
				throw new IllegalStateException("External storage isn't accesible");
			}
			dictionaryPath = karta.getPath() + File.separator + "dictionary.gz";	
			File file = new File(dictionaryPath);
			
			if(fileLength != -1 && file.exists() && file.length() == fileLength){
				// file does really exist
				downloadedJapDict = true;
				Log.i("ParserService", "Dictionary alredy downloaded.");
			}else{
				if(file.exists()){
					file.delete();
				}
				output = new FileOutputStream(dictionaryPath);
	            byte data[] = new byte[1024];
	            int total = 0;
	            int count;
	            int perc = 0;
	            Log.i("ParserService", "Starting downloading dictionary size: "+fileLength+"\n input stream: "+input.toString());
	            if(fileLength == -1){
                	mNotificationView.setProgressBar(R.id.ntification_progressBar, 0, 0, true);
                	mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_in_progress));
                	mNotification.contentView=mNotificationView;
	                mNotifyManager.notify(0, mNotification);
	            }
	            while ((count = input.read(data)) != -1){
	                total += count;
	                
	                // publishing the progress....

	                output.write(data, 0, count);
	                if(fileLength != -1){
		                int persPub = Math.round((((float)total/fileLength)*100)) ;
		                if(perc+4 < persPub){
		                	mNotificationView.setProgressBar(R.id.ntification_progressBar, 100, persPub, false);
		                	mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_in_progress) + " "+ persPub +" %");
		                	mNotification.contentView=mNotificationView;
			                mNotifyManager.notify(0, mNotification);
			                perc = persPub;
		                }
	                }
	            	if(canceled){
	            		closeIOStreams();
	            		return;
	            	}
	            }
	            Log.w("ParserService","downloading japdict: downloaded size: "+total);
	            output.flush(); 
	            closeIOStreams();
	            downloadedJapDict = true;
	            mNotificationView.setProgressBar(R.id.ntification_progressBar, 0, 0, false);
	            mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_complete));
	            mNotification.contentView = mNotificationView;
	            
	            mNotifyManager.notify(0, mNotification);
	            Log.i("ParserService", "Downloading dictionary finished");
			}

			
            
            
            //downloading kanjidict
    		url = null;
    		try{
    			url = new URL(ParserService.KANJIDICT_PATH);
    		}catch(MalformedURLException ex){
    			Log.e("ParserService", "Error: creating url for downloading kanjidict2");
    		}
    		if(url != null){
    			//kanjidict isnt necessary
	    		connection = null;
	    		fileLength = 0;
	    		
	    		mNotificationView.setTextViewText(R.id.notification_title, getString(R.string.dictionary_kanji_download_title));
	    		try{
	    			connection = url.openConnection();
	    			connection.setRequestProperty("Accept-Encoding", "identity");
					connection.connect();
					//velikost souboru
					fileLength = connection.getContentLength();
					input = new BufferedInputStream(connection.getInputStream(),1024);
					kanjiDictPath = karta.getPath() + File.separator + "kanjidict.gz";	
					File file_kanjidict = new File(kanjiDictPath);
					
					if(fileLength != -1 && file_kanjidict.exists() && file_kanjidict.length() == fileLength){
						// file does really exist
						downloadedKanjiDict = true;
						Log.i("ParserService", "Kaji Dictionary alredy downloaded.");
					}else{
						if(file_kanjidict.exists()){
							file_kanjidict.delete();
						}
						output = new FileOutputStream(kanjiDictPath);
			            byte data[] = new byte[1024];
			            int total = 0;
			            int count = 0;
			            int perc = 0;
			            Log.i("ParserService", "Starting downloading kanji dictionary size: "+fileLength+"\n input stream: "+input.toString());
			            if(fileLength == -1){
		                	mNotificationView.setProgressBar(R.id.ntification_progressBar, 0, 0, true);
		                	mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_in_progress));
		                	mNotification.contentView=mNotificationView;
			                mNotifyManager.notify(0, mNotification);
			            }
			            while ((count = input.read(data)) != -1){
			                total += count;
			                // publishing the progress....
			                output.write(data, 0, count);
			                if(fileLength != -1){
				                int persPub = Math.round((((float)total/fileLength)*100));
				                //System.out.println("pers: "+persPub);
				                if(perc+4 < persPub){
				                	mNotificationView.setProgressBar(R.id.ntification_progressBar, 100, persPub, false);
				                	mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_in_progress) + " "+ persPub +" %");
				                	mNotification.contentView=mNotificationView;
					                mNotifyManager.notify(0, mNotification);
					                perc = persPub;
				                }
			                }
			            	if(canceled){
			            		closeIOStreams();
			            		Log.i("ParserService", "canceled");
			            		return;
			            	}
			            }
			            Log.w("ParserService","downloading kanjidict: downloaded size: "+total);
			            output.flush(); 
			            closeIOStreams();
			            downloadedKanjiDict = true;
			            
			            mNotificationView.setProgressBar(R.id.ntification_progressBar, 0, 0, false);
			            mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_complete));
			            mNotification.contentView = mNotificationView;
			            
			            mNotifyManager.notify(0, mNotification);
			            Log.i("ParserService", "Downloading dictionaries finished");
					}
	    		}catch(IOException ex){
	    			Log.w("ParserService", "Error: kanjdiict2 file not found");
	    		}
    		}

    		String japDictAbsolutePath = null;
    		String japKanjiDictAbsolutePath = null;
			if(downloadedJapDict){
				japDictAbsolutePath = parseDictionary(dictionaryPath);
			}
			if(downloadedKanjiDict){
				japKanjiDictAbsolutePath = parseKanjiDict(kanjiDictPath);
			}
			if(japDictAbsolutePath!= null){
				serviceSuccessfullyDone(japDictAbsolutePath, japKanjiDictAbsolutePath);
			}else{
				Log.e("ParserService", "Parsing dictionary failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ParserService", "Exception: "+e.toString());
		}finally{
			closeIOStreams();
			
		}

		stopSelf();
	}
	
	
	private String parseDictionary(String path) throws InterruptedException, IOException, ParserConfigurationException, SAXException{
        
		mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_parsing_in_progress));
		mNotificationView.setTextViewText(R.id.notification_title, getString(R.string.parsing_downloaded_dictionary));
		mNotification.contentView = mNotificationView;
        mNotifyManager.notify(0, mNotification);
        
        Log.i("ParserService", "Parsing dictionary - start");
        
		File downloadedFile = new File(path);
		    
        InputStream parsFile = new GZIPInputStream(new FileInputStream(downloadedFile));
        //kodovani utf-8
        Reader reader = new InputStreamReader(parsFile,"UTF-8");
        InputSource is = new InputSource(reader);
        
        Log.i("ParserService", "Parsing dictionary - input streams created");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        
        String indexFile = getExternalCacheDir().getAbsolutePath() + File.separator + "dictionary";
        File file = new File(indexFile);
        boolean renameFolder = false;
        if(!file.mkdir()){
        	String indexFileTempPath = indexFile + "_temp";
        	file = new File(indexFileTempPath);
        	renameFolder = true;
        	if(!file.mkdir()){
        		deleteDirectory(file);
        		file.mkdir();
        	}	
        }

        Log.i("ParserService", "Parsing dictionary - index folder created");
        Log.i("ParserService", "Parsing dictionary - SAX ready");
        DefaultHandler handler = new SaxDataHolder(file,getApplicationContext(),mNotifyManager,mNotification,mNotificationView);
        
        
        try{
        	saxParser.parse(is,handler);
        	Log.i("ParserService", "Parsing dictionary - SAX ended");
        	downloadedFile.delete();
        	Log.i("ParserService", "Parsing dictionary - downloaded file deleted");
        	mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_complete));
        	mNotificationView.setProgressBar(R.id.ntification_progressBar, 0, 0, true);       	
        	mNotification.contentView = mNotificationView;
     
    		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		intent.putExtra(DICTIONARY_PREFERENCES, true);
    		PendingIntent resultPendingIntent =
    				PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    		mNotification.contentIntent = resultPendingIntent;
            mNotifyManager.notify(0, mNotification);
            
            
            complete = true;
            
            if(renameFolder){
            	Log.i("ParserService", "Parsing dictionary - rename folders");
            	File directory = new File(indexFile);
            	deleteDirectory(directory);
            	if(file.renameTo(directory)){
            		Log.i("ParserService", "Parsing dictionary - folder renamed");
            		file = directory;
            	}
            }
            
    		return file.getAbsolutePath();
    		
        }catch(SAXException ex){
        	Log.e("ParserService", "SaxDataHolder: "+ex.getMessage());
        	stopSelf();
        }catch(Exception ex){
        	Log.e("ParserService", "SaxDataHolder - Unknown exception: "+ex.toString());
        	stopSelf();
        }
		return null;

	}
	
	private String parseKanjiDict(String path) 
			throws InterruptedException, IOException, ParserConfigurationException, SAXException{
		Log.i("ParserService","Parsing kanji dict");
        
		mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_parsing_in_progress));
		mNotification.contentView = mNotificationView;
        mNotifyManager.notify(0, mNotification);
        
        Log.i("ParserService", "Parsing KanjiDict - start");
        
		File downloadedFile = new File(path);
		    
        InputStream parsFile = new GZIPInputStream(new FileInputStream(downloadedFile));
        //kodovani utf-8
        Reader reader = new InputStreamReader(parsFile,"UTF-8");
        InputSource is = new InputSource(reader);
        
        Log.i("ParserService", "Parsing KanjiDict - input streams created");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        
        String indexFile = getExternalCacheDir().getAbsolutePath() + File.separator + "kanjidict";
        File file = new File(indexFile);
        boolean renameFolder = false;
        if(!file.mkdir()){
        	String indexFileTempPath = indexFile + "_temp";
        	file = new File(indexFileTempPath);
        	renameFolder = true;
        	if(!file.mkdir()){
        		deleteDirectory(file);
        		file.mkdir();
        	}	
        }

        Log.i("ParserService", "Parsing KanjiDict - index folder created");
        Log.i("ParserService", "Parsing KanjiDict - SAX ready");
        DefaultHandler handler = new SaxDataHolderKanjiDict(file,getApplicationContext(),mNotifyManager,mNotification,mNotificationView);
        
        
        try{
        	saxParser.parse(is,handler);
        	Log.i("ParserService", "Parsing KanjiDict - SAX ended");
        	downloadedFile.delete();
        	Log.i("ParserService", "Parsing KanjiDict - downloaded file deleted");
        	mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_complete));
        	mNotificationView.setProgressBar(R.id.ntification_progressBar, 0, 0, true);       	
        	mNotification.contentView = mNotificationView;
     
    		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		intent.putExtra(DICTIONARY_PREFERENCES, true);
    		PendingIntent resultPendingIntent =
    				PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    		mNotification.contentIntent = resultPendingIntent;
            mNotifyManager.notify(0, mNotification);
            
            
            complete = true;
            
            if(renameFolder){
            	Log.i("ParserService", "Parsing KanjiDict - rename folders");
            	File directory = new File(indexFile);
            	deleteDirectory(directory);
            	if(file.renameTo(directory)){
            		Log.i("ParserService", "Parsing KanjiDict - folder renamed");
            		file = directory;
            	}
            	
            }			
            
    		return file.getAbsolutePath();
    		
        }catch(SAXException ex){
        	Log.e("ParserService", "SaxDataHolderKanjiDict: "+ex.getMessage());
        	stopSelf();
        }catch(Exception ex){
        	Log.e("ParserService", "SaxDataHolderKanjiDict - Unknown exception: "+ex.toString());
        	stopSelf();
        }
		return null;

	}
	
	
	private void serviceSuccessfullyDone(String dictionaryPath,String kanjiDictPath){
		Log.i("ParserService", "Parsing dictionary - parsing succesfully done, saving preferences");
        SharedPreferences settings = getSharedPreferences(DICTIONARY_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("hasValidDictionary", true);
		Log.i("ParserService", "Dictionary path: "+dictionaryPath);
		Log.i("ParserService", "KanjiDict path: "+kanjiDictPath);
        editor.putString("pathToDictionary", dictionaryPath);     
        editor.putBoolean("hasValidKanjiDictionary", true);
        editor.putString("pathToKanjiDictionary", kanjiDictPath);  
        Date date = new Date();
        editor.putLong("dictionaryLastUpdate",date.getTime());
        editor.commit();
        
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean english = sharedPrefs.getBoolean("language_english", false);
        boolean french = sharedPrefs.getBoolean("language_french", false);        
        boolean dutch = sharedPrefs.getBoolean("language_dutch", false);
        boolean german = sharedPrefs.getBoolean("language_german", false);
        if(!english && !french && !dutch && !german){
        	Log.i("ParserService","Setting english as only translation language");
            SharedPreferences.Editor editor_lang = sharedPrefs.edit();
            editor_lang.putBoolean("language_english", true);
            editor_lang.commit();
        }
        
        Log.i("ParserService", "Parsing dictionary - preferences saved");
        Intent intent = new Intent("downloadingDictinaryServiceDone");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);	
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		closeIOStreams();
		if(!complete){
			mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_interrupted));
			mNotification.contentView = mNotificationView;
            mNotifyManager.notify(0, mNotification);
			Intent intent = new Intent("serviceCanceled");
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);	
        	Log.w("ParserService", "Service ending none complete");
		}
	}
	
	private void closeIOStreams(){
		if(input != null){
			try {
				input.close();
			} catch (IOException e) {}
			input = null;
		}
		if(output != null){
			try {
				output.close();
			} catch (IOException e) {}
			output = null;
		}
		
	}
	
	
	/**
	 * Deletes given directory 
	 * 
	 * @param directory directory to be deleted 
	 * @return true on succes
	 */
	static private boolean deleteDirectory(File directory) {
	    if( directory.exists() ) {
	      File[] files = directory.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( directory.delete() );
	  }
	

}
