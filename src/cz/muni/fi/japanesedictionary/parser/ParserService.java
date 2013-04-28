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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.main.MainActivity;

/**
 * Service for downloading and aprsing dictionaries.
 * 
 * @author Jaroslav Klech
 * 
 */
public class ParserService extends IntentService {

	public static final String DICTIONARY_PATH = "http://ftp.monash.edu.au/pub/nihongo/JMdict.gz";
	public static final String KANJIDICT_PATH = "http://www.csse.monash.edu.au/~jwb/kanjidic2/kanjidic2.xml.gz";
	public static final String DICTIONARY_PREFERENCES = "cz.muni.fi.japanesedictionary";
	
	private URL mDownloadJMDictFrom = null;
	private File mDownloadJMDictTo = null;
	private boolean mDownloadingJMDict = false;
	private URL mDownloadKanjidicFrom = null;
	private File mDownloadKanjidicTo = null;
	private boolean mDownloadingKanjidic = false;
	
	private boolean mDownloadInProgress = false;
	private boolean mCurrentlyDownloading = false;
	private boolean mParsing = false;

	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotifyManager = null;
	private Notification mNotification = null;
	private RemoteViews mNotificationView = null;
	private boolean mComplete = false;


    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;
    private boolean mRedelivery;
    private int mStartId;

    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
        	mStartId = msg.arg1;
            onHandleIntent((Intent)msg.obj);
        }
    }


    private class ServiceDownloadHandler extends ServiceHandler {
        public ServiceDownloadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
			try {
				downloadDictionaries();
			} catch (IOException e) {
				Log.w("ParserService","IOException caught while downloading: "+e.toString());
				stopSelf(mStartId);
			}
        }
    }
    /**
     * Sets intent redelivery preferences.  Usually called from the constructor
     * with your preferred semantics.
     *
     * <p>If enabled is true,
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_REDELIVER_INTENT}, so if this process dies before
     * {@link #onHandleIntent(Intent)} returns, the process will be restarted
     * and the intent redelivered.  If multiple Intents have been sent, only
     * the most recent one is guaranteed to be redelivered.
     *
     * <p>If enabled is false (the default),
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_NOT_STICKY}, and if the process dies, the Intent
     * dies along with it.
     */
    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }
    
	private BroadcastReceiver mInternetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
	        ConnectivityManager conn =  (ConnectivityManager)
	                context.getSystemService(Context.CONNECTIVITY_SERVICE);
	            NetworkInfo networkInfo = conn.getActiveNetworkInfo();
			if (networkInfo == null) {
				Log.i("ParserService","Connection lost");
			}else{
				Log.i("ParserService","Connection Established");
				if(mDownloadInProgress && !mCurrentlyDownloading){
			        Message msg = mServiceHandler.obtainMessage();
			        mServiceHandler = new ServiceDownloadHandler(mServiceLooper);
			        mServiceHandler.sendMessage(msg);
				}
			}
		}
	};

	public ParserService() {
		super("ParserService");
	}	
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
        thread.start();
        
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
		setIntentRedelivery(true);

		
	}

    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    
	/**
	 * Doenloads file from given URL. Creates new file or append old one.
	 * 
	 * @param url - to download from
	 * @param outputFile File to save from URL
	 * @return true if file was downloaded succesfully else false
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private boolean downloadFile(URL url, File outputFile) 
			throws IOException
			{
		mCurrentlyDownloading = true;
		BufferedInputStream input = null;
		OutputStream output = null;
		
		HttpURLConnection connection = null;
		
		connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Accept-Encoding", "identity");
		connection.connect();
		long fileLength = connection.getContentLength();
		long total = 0;

		
		if(outputFile.exists()){
			output = new FileOutputStream(outputFile.getPath(), true);
			total = outputFile.length();
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("Accept-Encoding", "identity");
			connection.setRequestProperty("Range", "bytes=" + total + "-");
			connection.connect();
		}else{
			output = new FileOutputStream(outputFile.getPath());
		}
		

		// velikost souboru

		System.out.println("size: "+fileLength);
		input = new BufferedInputStream(connection.getInputStream(), 1024);
		
		if (input == null || output == null) {
			Log.e("ParserService",
					"Error while downloading file, one of streams is null");
			closeIOStreams(input, output);
			mCurrentlyDownloading = false;
			return false;
		}
		
		if (fileLength == -1) {
			mNotificationView.setProgressBar(
					R.id.ntification_progressBar, 0, 0, true);
			mNotificationView
					.setTextViewText(
							R.id.notification_text,
							getString(R.string.dictionary_download_in_progress));
			mNotification.contentView = mNotificationView;
			mNotifyManager.notify(0, mNotification);
		}
		
		
		
		
		byte data[] = new byte[1024];

		int count = 0;
		int perc = 0;
		long lastUpdate = System.currentTimeMillis();
		try{
			while ((count = input.read(data)) != -1) {
					total += count;
	
					output.write(data, 0, count);
					// publishing the progress....
					if (fileLength != -1) {
						
						long current = System.currentTimeMillis();
						if (lastUpdate + 500 < current) {
							int persPub = Math.round((((float) total / fileLength) * 100));
							if(perc < persPub){
								lastUpdate = current;
								System.out.println(lastUpdate);
								
								mNotificationView.setProgressBar(
										R.id.ntification_progressBar, 100, persPub, false);
								mNotificationView.setTextViewText(R.id.notification_text,
										getString(R.string.dictionary_download_in_progress));
								mNotification.contentView = mNotificationView;
								mNotifyManager.notify(0, mNotification);
								perc = persPub;
							}
						}
					}
			}
		}catch(IOException ex){
			Log.w("ParserService", "ConnectionLost: "+ex);
			closeIOStreams(input, output);
			mCurrentlyDownloading = false;
			return false;
		}
		mCurrentlyDownloading = false;
		closeIOStreams(input, output);
		return true;
		
	}

	/**
	 * Downloads dictionaries and launches parseDictionaries()
	 * 
	 * @throws IOException
	 */
	private void downloadDictionaries() throws IOException{
		if(mDownloadingJMDict){
			mNotificationView.setViewVisibility(R.id.ntification_progressBar,
					View.VISIBLE);
			if(downloadFile(mDownloadJMDictFrom,mDownloadJMDictTo)){
				mDownloadingJMDict = false;
				mDownloadingKanjidic = true;
			}else{
				return;
			}
		} 
		if(mDownloadingKanjidic){
			mNotificationView.setTextViewText(R.id.notification_title,
					getString(R.string.dictionary_kanji_download_title));
			mNotificationView.setViewVisibility(R.id.ntification_progressBar,
					View.VISIBLE);
			if(downloadFile(mDownloadKanjidicFrom,mDownloadKanjidicTo)){
				mDownloadingKanjidic = false;
				mDownloadInProgress = false;
				
				mNotificationView.setProgressBar(
						R.id.ntification_progressBar, 0, 0, false);
				mNotificationView.setViewVisibility(R.id.ntification_progressBar,
						View.GONE);
				mNotificationView.setTextViewText(R.id.notification_text,
						getString(R.string.dictionary_download_complete));
				mNotification.contentView = mNotificationView;

				mNotifyManager.notify(0, mNotification);
				Log.i("ParserService", "Downloading dictionary finished");
				
			}else{
				return;
			}
		}
		if(!mDownloadInProgress && !mParsing){
			try {
				mParsing = true;
				parseDictionaries();
			} catch (ParserConfigurationException e) {
				Log.e("ParserService","ParserConfigurationException exception occured: "+e.toString());
				stopSelf(mStartId);
			} catch (SAXException e) {
				Log.e("ParserService","SAXException exception occured: "+e.toString());
				stopSelf(mStartId);
			}
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
		this.unregisterReceiver(mInternetReceiver);
		mInternetReceiver = null;
		
		String japDictAbsolutePath = parseDictionary(mDownloadJMDictTo.getPath());
		String japKanjiDictAbsolutePath = parseKanjiDict(mDownloadKanjidicTo.getPath());
		

		if (japDictAbsolutePath != null) {
			serviceSuccessfullyDone(japDictAbsolutePath,japKanjiDictAbsolutePath);
		} else {
			Log.e("ParserService", "Parsing dictionary failed");
		}
	}
	
	/**
	 * Downloads dictionaries.
	 */
	@Override
	protected void onHandleIntent(Intent arg0) {		
		
		Log.i("ParserService", "Creating parser service");
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationView = new RemoteViews(this.getPackageName(),
				R.layout.notification);
		mNotificationView.setImageViewResource(R.id.notification_image,
				R.drawable.ic_launcher);
		mNotificationView.setTextViewText(R.id.notification_title,
				getString(R.string.dictionary_download_title));
		mNotificationView.setTextViewText(R.id.notification_text,
				getString(R.string.dictionary_download_in_progress));

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
		mNotifyManager.notify(0, mNotification);
		
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
		
		
		
		
		String dictionaryPath = null;
		String kanjiDictPath = null;

		URL url = null;
		try {
			url = new URL(ParserService.DICTIONARY_PATH);
		} catch (MalformedURLException ex) {
			Log.e("ParserService",
					"Error: creating url for downloading dictionary");
			return;
		}



		try {
			File karta = null;
			if (MainActivity.canWriteExternalStorage()) {
				// je dostupna karta
				karta = getExternalCacheDir();
			}
			if (karta == null) {
				throw new IllegalStateException(
						"External storage isn't accesible");
			}
			dictionaryPath = karta.getPath() + File.separator + "dictionary.gz";
			File outputFile = new File(dictionaryPath);
			if(outputFile.exists()){
				outputFile.delete();
			}

			mDownloadJMDictFrom = url;
			mDownloadJMDictTo = outputFile;
			mDownloadingJMDict = true;
			mDownloadInProgress = true;
			
			// downloading kanjidict
			url = null;
			try {
				url = new URL(ParserService.KANJIDICT_PATH);
			} catch (MalformedURLException ex) {
				Log.e("ParserService",
						"Error: creating url for downloading kanjidict2");
			}
			if (url != null) {
				kanjiDictPath = karta.getPath() + File.separator
						+ "kanjidict.gz";
				File fileKanjidict = new File(kanjiDictPath);
				if(fileKanjidict.exists()){
					fileKanjidict.delete();
				}
				mDownloadingKanjidic = false;
				mDownloadKanjidicFrom = url;
				mDownloadKanjidicTo = fileKanjidict;
			}
			
			
			downloadDictionaries();
			
		} catch(MalformedURLException e){
			Log.e("ParserService", "MalformedURLException wrong format of URL: " + e.toString());
			stopSelf(mStartId);
		} catch(IOException e){
			Log.e("ParserService", "IOException downloading interrupted: " + e.toString());
			stopSelf(mStartId);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ParserService", "Exception: " + e.toString());
			stopSelf(mStartId);
		}

		
	}

	/**
	 * Parse downloaded JMdict dictionary.
	 * 
	 * @param path to the dictionary gziped file
	 * @return path to lucene folder for jmdict
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private String parseDictionary(String path) throws 
			IOException, ParserConfigurationException, SAXException {

		mNotificationView.setTextViewText(R.id.notification_text,
				getString(R.string.dictionary_parsing_in_progress));
		mNotificationView.setViewVisibility(R.id.ntification_progressBar,
				View.VISIBLE);
		mNotificationView.setTextViewText(R.id.notification_title,
				getString(R.string.parsing_downloaded_dictionary));
		mNotification.contentView = mNotificationView;
		mNotifyManager.notify(0, mNotification);

		Log.i("ParserService", "Parsing dictionary - start");

		File downloadedFile = new File(path);

		InputStream parsFile = new GZIPInputStream(new FileInputStream(
				downloadedFile));
		// kodovani utf-8
		Reader reader = new InputStreamReader(parsFile, "UTF-8");
		InputSource is = new InputSource(reader);

		Log.i("ParserService", "Parsing dictionary - input streams created");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		String indexFile = getExternalCacheDir().getAbsolutePath()
				+ File.separator + "dictionary";
		File file = new File(indexFile);
		boolean renameFolder = false;
		if (!file.mkdir()) {
			String indexFileTempPath = indexFile + "_temp";
			file = new File(indexFileTempPath);
			renameFolder = true;
			if (!file.mkdir()) {
				deleteDirectory(file);
				file.mkdir();
			}
		}

		Log.i("ParserService", "Parsing dictionary - index folder created");
		Log.i("ParserService", "Parsing dictionary - SAX ready");
		DefaultHandler handler = new SaxDataHolder(file,
				getApplicationContext(), mNotifyManager, mNotification,
				mNotificationView);

		try {
			saxParser.parse(is, handler);
			Log.i("ParserService", "Parsing dictionary - SAX ended");
			downloadedFile.delete();
			Log.i("ParserService",
					"Parsing dictionary - downloaded file deleted");
			mNotificationView.setTextViewText(R.id.notification_text,
					getString(R.string.dictionary_download_complete));
			mNotificationView.setProgressBar(R.id.ntification_progressBar, 0,
					0, true);
			mNotification.contentView = mNotificationView;
			mNotifyManager.notify(0, mNotification);

			mComplete = true;

			if (renameFolder) {
				Log.i("ParserService", "Parsing dictionary - rename folders");
				File directory = new File(indexFile);
				deleteDirectory(directory);
				if (file.renameTo(directory)) {
					Log.i("ParserService",
							"Parsing dictionary - folder renamed");
					file = directory;
				}
			}

			return file.getAbsolutePath();

		} catch (SAXException ex) {
			Log.e("ParserService", "SaxDataHolder: " + ex.getMessage());
			stopSelf(mStartId);
		} catch (Exception ex) {
			Log.e("ParserService",
					"SaxDataHolder - Unknown exception: " + ex.toString());
			stopSelf(mStartId);
		}
		return null;

	}

	/**
	 * Parse downloaded KanjiDict2 dictionary.
	 * 
	 * @param path to the KanjiDict2 dictionary gziped file
	 * @return path to lucene folder for kanjidict2
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private String parseKanjiDict(String path) throws
			IOException, ParserConfigurationException, SAXException {
		Log.i("ParserService", "Parsing kanji dict");

		mNotificationView.setTextViewText(R.id.notification_text,
				getString(R.string.dictionary_parsing_in_progress));
		mNotification.contentView = mNotificationView;
		mNotifyManager.notify(0, mNotification);

		Log.i("ParserService", "Parsing KanjiDict - start");

		File downloadedFile = new File(path);

		InputStream parsFile = new GZIPInputStream(new FileInputStream(
				downloadedFile));
		// kodovani utf-8
		Reader reader = new InputStreamReader(parsFile, "UTF-8");
		InputSource is = new InputSource(reader);

		Log.i("ParserService", "Parsing KanjiDict - input streams created");
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		String indexFile = getExternalCacheDir().getAbsolutePath()
				+ File.separator + "kanjidict";
		File file = new File(indexFile);
		boolean renameFolder = false;
		if (!file.mkdir()) {
			String indexFileTempPath = indexFile + "_temp";
			file = new File(indexFileTempPath);
			renameFolder = true;
			if (!file.mkdir()) {
				deleteDirectory(file);
				file.mkdir();
			}
		}

		Log.i("ParserService", "Parsing KanjiDict - index folder created");
		Log.i("ParserService", "Parsing KanjiDict - SAX ready");
		DefaultHandler handler = new SaxDataHolderKanjiDict(file,
				getApplicationContext(), mNotifyManager, mNotification,
				mNotificationView);

		try {
			saxParser.parse(is, handler);
			Log.i("ParserService", "Parsing KanjiDict - SAX ended");
			downloadedFile.delete();
			Log.i("ParserService",
					"Parsing KanjiDict - downloaded file deleted");
			mNotificationView.setTextViewText(R.id.notification_text,
					getString(R.string.dictionary_download_complete));
			mNotificationView.setProgressBar(R.id.ntification_progressBar, 0,
					0, true);
			mNotificationView.setViewVisibility(R.id.ntification_progressBar,
					View.GONE);
			mNotification.contentView = mNotificationView;
			mNotifyManager.notify(0, mNotification);

			mComplete = true;

			if (renameFolder) {
				Log.i("ParserService", "Parsing KanjiDict - rename folders");
				File directory = new File(indexFile);
				deleteDirectory(directory);
				if (file.renameTo(directory)) {
					Log.i("ParserService", "Parsing KanjiDict - folder renamed");
					file = directory;
				}

			}

			return file.getAbsolutePath();

		} catch (SAXException ex) {
			Log.e("ParserService", "SaxDataHolderKanjiDict: " + ex.getMessage());
			stopSelf(mStartId);
		} catch (Exception ex) {
			Log.e("ParserService",
					"SaxDataHolderKanjiDict - Unknown exception: "
							+ ex.toString());
			stopSelf(mStartId);
		}
		return null;

	}

	/**
	 * Called when parsing was succesfully done. Sets shared preferences and
	 * broadcast downloadingDictinaryServiceDone intent.
	 * 
	 * @param dictionaryPath path to JMdict dictionary
	 * @param kanjiDictPath path to kanjidict2 dictionary
	 */
	private void serviceSuccessfullyDone(String dictionaryPath,
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
		stopSelf(mStartId);
	}

	/**
	 * Closes IO streams. If service wasn't done succesfully changes
	 * notification and broadcasts serviceCanceled intent.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopForeground(true);
		if(mInternetReceiver!= null){
			this.unregisterReceiver(mInternetReceiver);
		}
		Log.w("ParserService", "restarting notificatiomn, setting ongoing false");
		mBuilder.setAutoCancel(true);
		mBuilder.setOngoing(false);
		mBuilder.setContent(mNotificationView);
		mNotification = mBuilder.build();	
		mNotifyManager.notify(0, mNotification);
		if (!mComplete) {
			mNotificationView.setTextViewText(R.id.notification_text,
					getString(R.string.dictionary_download_interrupted));
			mNotification.contentView = mNotificationView;
			mNotifyManager.notify(0, mNotification);
			Intent intent = new Intent("serviceCanceled");
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.w("ParserService", "Service ending none complete");
		}
		mServiceLooper.quit();
		
	}

	/**
	 * Tries to close IO streams.
	 */
	private void closeIOStreams(InputStream input, OutputStream output) {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
			}
			input = null;
		}
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
			}
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
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (directory.delete());
	}

}
