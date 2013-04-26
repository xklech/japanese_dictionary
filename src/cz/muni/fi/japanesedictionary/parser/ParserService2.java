package cz.muni.fi.japanesedictionary.parser;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.main.MainActivity;

public class ParserService2 extends Service {

		public static final String DICTIONARY_PATH = "http://ftp.monash.edu.au/pub/nihongo/JMdict.gz";
		public static final String KANJIDICT_PATH = "http://www.csse.monash.edu.au/~jwb/kanjidic2/kanjidic2.xml.gz";
		public static final String DICTIONARY_PREFERENCES = "cz.muni.fi.japanesedictionary";
		
		private File mDownloadJMDictTo = null;
		private File mDownloadKanjidicTo = null;
		
		private boolean mAppend = false;
		
		private boolean mDownloadNeeded = true;
		private boolean mCurrentlyDownloading = false;
	
		private NotificationCompat.Builder mBuilder;
		private NotificationManager mNotifyManager = null;
		private Notification mNotification = null;
		private RemoteViews mNotificationView = null;
		private boolean mComplete = false;
	
		private BroadcastReceiver mInternetReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent intent) {
		        boolean noConnectivity = intent.getBooleanExtra(
		                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);;
				if (noConnectivity) {					
					Log.i("ParserService","Connection lost");
				}else{
					if(mDownloadNeeded && !mCurrentlyDownloading){
						  DownloadAsyncTask downloadAsyncTask = 
								  new DownloadAsyncTask(mNotification, mNotificationView, 
										  mNotifyManager, mAppend, ParserService2.this);
						  downloadAsyncTask.execute();
					}
					Log.i("ParserService","Connection established");
				}
			}
		};
		
	  @Override
	  public void onCreate() {

			super.onCreate();
			Log.i("ParserService", "Creating parser service");
			mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			mNotificationView = new RemoteViews(this.getPackageName(),
					R.layout.notification);
			mNotificationView.setImageViewResource(R.id.notification_image,
					R.drawable.ic_launcher);
			mNotificationView.setTextViewText(R.id.notification_title,
					getString(R.string.dictionary_download_title));
			mNotificationView.setTextViewText(R.id.notification_text,
					getString(R.string.dictionary_download_in_progress) + " 0 %");

			mBuilder = new NotificationCompat.Builder(
					this);
			mBuilder.setAutoCancel(false);
			mBuilder.setOngoing(true);
			mBuilder.setSmallIcon(R.drawable.ic_launcher);

			Intent resultIntent = new Intent(getApplicationContext(),
					MainActivity.class);
			resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, resultIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);

			mNotification = mBuilder.build();
			mNotification.icon = R.drawable.ic_launcher;
			mNotification.contentView = mNotificationView;
			
			startForeground(0,mNotification);
			
			this.registerReceiver(mInternetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

			
			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			boolean english = sharedPrefs.getBoolean("language_english", false);
			boolean french = sharedPrefs.getBoolean("language_french", false);
			boolean dutch = sharedPrefs.getBoolean("language_dutch", false);
			boolean german = sharedPrefs.getBoolean("language_german", false);
			if (!english && !french && !dutch && !german) {
				Log.i("ParserService",
						"Setting english as only translation language");
				SharedPreferences.Editor editor_lang = sharedPrefs.edit();
				editor_lang.putBoolean("language_english", true);
				editor_lang.commit();
			}

	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
		  mCurrentlyDownloading = true;
		  DownloadAsyncTask downloadAsyncTask = 
				  new DownloadAsyncTask(mNotification, mNotificationView, 
						  mNotifyManager, mAppend, this);
		  downloadAsyncTask.execute();

		  
		  
	      return START_STICKY;
	  }

	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
	
	/**
	 * Called when parsing was succesfully done. Sets shared preferences and
	 * broadcast downloadingDictinaryServiceDone intent.
	 * 
	 * @param dictionaryPath path to JMdict dictionary
	 * @param kanjiDictPath path to kanjidict2 dictionary
	 */
	public void serviceSuccessfullyDone(String dictionaryPath,
			String kanjiDictPath) {
		Log.i("ParserService",
				"Parsing dictionary - parsing succesfully done, saving preferences");
		SharedPreferences settings = getSharedPreferences(
				DICTIONARY_PREFERENCES, 0);
		SharedPreferences.Editor editor = settings.edit();
		Log.i("ParserService", "Dictionary path: " + dictionaryPath);
		Log.i("ParserService", "KanjiDict path: " + kanjiDictPath);
		editor.putString("pathToDictionary", dictionaryPath);
		editor.putString("pathToKanjiDictionary", kanjiDictPath);
		Date date = new Date();
		editor.putLong("dictionaryLastUpdate", date.getTime());
		editor.commit();

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean english = sharedPrefs.getBoolean("language_english", false);
		boolean french = sharedPrefs.getBoolean("language_french", false);
		boolean dutch = sharedPrefs.getBoolean("language_dutch", false);
		boolean german = sharedPrefs.getBoolean("language_german", false);
		if (!english && !french && !dutch && !german) {
			Log.i("ParserService",
					"Setting english as only translation language");
			SharedPreferences.Editor editor_lang = sharedPrefs.edit();
			editor_lang.putBoolean("language_english", true);
			editor_lang.commit();
		}

		Log.i("ParserService", "Parsing dictionary - preferences saved");
		Intent intent = new Intent("downloadingDictinaryServiceDone");
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		stopSelf();
	}
	
	/**
	 * Closes IO streams. If service wasn't done succesfully changes
	 * notification and broadcasts serviceCanceled intent.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mInternetReceiver);
		mBuilder.setAutoCancel(true);
		mBuilder.setOngoing(false);
		mNotification = mBuilder.build();
		if (!mComplete) {
			mNotificationView.setTextViewText(R.id.notification_text,
					getString(R.string.dictionary_download_interrupted));
			mNotification.contentView = mNotificationView;
			mNotifyManager.notify(0, mNotification);
			Intent intent = new Intent("serviceCanceled");
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.w("ParserService", "Service ending none complete");
		}
	}
	
	
	
	public void setAppend(boolean append){
		mAppend = append;
	}
	
	public void setComplete(boolean complete){
		mComplete = complete;
	}
	
	public void setCurrentlyDownloading(boolean value){
		mCurrentlyDownloading = value;
	}
	
	public void setDownloadedJmDict(File downlaoded){
		mDownloadJMDictTo = downlaoded;
	}
	
	public void setDownloadedKanjidic(File downlaoded){
		mDownloadKanjidicTo = downlaoded;
	}
	
	public void downloadingSuccesfullyDone(){
		mCurrentlyDownloading = false;
		mDownloadNeeded = false;
		mNotificationView.setProgressBar(
				R.id.ntification_progressBar, 0, 0, false);
		mNotificationView.setViewVisibility(R.id.ntification_progressBar,
				View.GONE);
		mNotificationView.setTextViewText(R.id.notification_text,
				getString(R.string.dictionary_download_complete));
		mNotification.contentView = mNotificationView;

		mNotifyManager.notify(0, mNotification);
		Log.i("ParserService", "Downloading dictionary finished");
		
		try {
			parseDictionaries();
		} catch (IOException e) {
			Log.e("ParserService","IOException caught, problem with dictionary files: "+e);
			stopSelf();
		} catch (ParserConfigurationException e) {
			Log.e("ParserService","ParserConfigurationException caught, problem with SAX parser: "+e);
			stopSelf();
		} catch (SAXException e) {
			Log.e("ParserService","SAXException caught, problem with SAX parser: "+e);
			stopSelf();
		}
		
	}
	
	/**
	 * Parses downloaded dictionaries
	 * 
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void parseDictionaries() throws IOException, ParserConfigurationException, SAXException{
		
		if(mDownloadJMDictTo == null || !mDownloadJMDictTo.exists() || mDownloadKanjidicTo == null || !mDownloadKanjidicTo.exists()){
			throw new IOException("One of dictionary files is doesn't exist");
		}
		
		ParseDictionariesAsyncTask parseDictionaryAsynctask = 
				new ParseDictionariesAsyncTask(mNotification, mNotificationView, mNotifyManager, this);
		parseDictionaryAsynctask.execute(mDownloadJMDictTo.getPath(),mDownloadKanjidicTo.getPath());

	}
	
	
	

	
}
