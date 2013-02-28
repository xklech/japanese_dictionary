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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.main.MainActivity;



public class ParserService extends IntentService{
	
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
		URL url = null;
		try{
			url = new URL(MainActivity.DICTIONARY_PATH);
		}catch(MalformedURLException ex){
			Log.e("ParserService", "Error: creating url for download");
		}
		
		URLConnection connection = null;
		long fileLength;
		
		String path;
		try {
			connection = url.openConnection();
			connection.connect();
			//velikost souboru
			fileLength = connection.getContentLength();
			input = new BufferedInputStream(url.openStream(),1024);
			
			File karta = null;
			if(MainActivity.canWriteExternalStorage()){
				//je dostupna karta
				karta = getExternalCacheDir();
			}
			if(karta == null){
				karta = getCacheDir();
			}
			path = karta.getPath() + File.separator + "dictionary.gz";	
			File file = new File(path);
			
			if(file.exists()){
				if(file.length() == fileLength){
					// file does really exist
					Log.i("ParserService", "Dictionary alredy downloaded.");
					parseDictionary(path);
					return;
					// TODO !!
				}
			}
			
            output = new FileOutputStream(path);
            byte data[] = new byte[1024];
            int total = 0;
            int count;
            int perc = 0;
            Log.i("ParserService", "Starting downloading dictionary size: "+fileLength);
            while ((count = input.read(data)) != -1){
                total += count;
                
                // publishing the progress....

                output.write(data, 0, count);
                int persPub = Math.round((((float)total/fileLength)*100)) ;
                if(perc < persPub){
                	mNotificationView.setProgressBar(R.id.ntification_progressBar, 100, persPub, false);
                	mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_in_progress) + " "+ persPub +" %");
                	mNotification.contentView=mNotificationView;
	                mNotifyManager.notify(0, mNotification);
	                perc = persPub;
                }
            	if(canceled){
            		closeIOStreams();
            		return;
            	}
            }
            output.flush(); 
          
            mNotificationView.setProgressBar(R.id.ntification_progressBar, 0, 0, false);
            mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_download_complete));
            mNotification.contentView = mNotificationView;
            
            mNotifyManager.notify(0, mNotification);
            Log.i("ParserService", "Downloading dictionary finished");
            parseDictionary(path);
            
            
            
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ParserService", "Exception: "+e.toString());
		}finally{
			closeIOStreams();
			
		}

		stopSelf();
	}
	
	
	private void parseDictionary(String path) throws InterruptedException, IOException, ParserConfigurationException, SAXException{
        
		mNotificationView.setTextViewText(R.id.notification_text, getString(R.string.dictionary_parsing_in_progress));
		mNotification.contentView = mNotificationView;
        mNotifyManager.notify(0, mNotification);
        
        Log.i("ParserService", "Parsing dictionary - start");
        
		File downloadedFile = new File(path);
		
		InputStream input = new FileInputStream(downloadedFile);        
        InputStream parsFile = new GZIPInputStream(input);
        //kodovani utf-8
        Reader reader = new InputStreamReader(parsFile,"UTF-8");
        InputSource is = new InputSource(reader);
        
        Log.i("ParserService", "Parsing dictionary - input streams created");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        
        String indexFile = getExternalCacheDir().getAbsolutePath() + File.separator + "dictionary";
        File file = new File(indexFile);
        if(!file.mkdir()){
        	deleteDirectory(file);
        	file.mkdir();	
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
        	mNotification.contentView = mNotificationView;
     
    		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		intent.putExtra(DICTIONARY_PREFERENCES, true);
    		PendingIntent resultPendingIntent =
    				PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    		mNotification.contentIntent = resultPendingIntent;
            mNotifyManager.notify(0, mNotification);
            
            
            complete = true;
            
    		serviceSuccessfullyDone(file.getAbsolutePath());
    		
        }catch(SAXException ex){
        	Log.e("ParserService", "SaxDataHolder: "+ex.getMessage());
        	stopSelf();
        }catch(Exception ex){
        	Log.e("ParserService", "SaxDataHolder - Unknown exception: "+ex.toString());
        	stopSelf();
        }

	}
	
	
	private void serviceSuccessfullyDone(String path){
		Log.i("ParserService", "Parsing dictionary - parsing succesfully done, saving preferences");
        SharedPreferences settings = getSharedPreferences(DICTIONARY_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("hasValidDictionary", true);
        editor.putString("pathToDictionary", path);        
        Date date = new Date();
        editor.putLong("dictionaryLastUpdate",date.getTime());
        editor.commit();
        Log.i("ParserService", "Parsing dictionary - preferences saved");
        Intent intent = new Intent("serviceDone");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
		canceled=true;
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
	
	
	//delete directory
	static private boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }

}
