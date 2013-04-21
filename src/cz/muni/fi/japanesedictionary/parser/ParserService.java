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


	private boolean connected = true;

	private NotificationManager mNotifyManager = null;
	private Notification mNotification = null;
	private RemoteViews mNotificationView = null;
	private boolean complete = false;

	private BroadcastReceiver internetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
	        boolean noConnectivity = intent.getBooleanExtra(
	                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);;
			if (noConnectivity) {
				connected = false;
				Log.i("ParserService","Connection lost");
			}else{
				connected = true;
				Log.i("ParserService","Connection established");
			}
		}
	};

	public ParserService() {
		super("ParserService");
	}

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

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this);
		mBuilder.setAutoCancel(true);
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
		mNotifyManager.notify(0, mNotification);
		
		this.registerReceiver(internetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		
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
			throws InterruptedException, IOException
			{
		
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
		long lastUpdate = (new Date()).getTime();
		while (true) {
			if (!connected) {
				closeIOStreams(input, output);
				return false;
			}
			try{
				count = input.read(data);
				if(count == -1){
					output.flush();
					closeIOStreams(input, output);
					return true;
				}
				total += count;

				output.write(data, 0, count);
				// publishing the progress....
				if (fileLength != -1) {
					
					long current = (new Date()).getTime();
					int persPub = Math.round((((float) total / fileLength) * 100));
					if (lastUpdate + 500 < current && perc < persPub) {
						lastUpdate = current;
						System.out.println(lastUpdate);
						
						mNotificationView.setProgressBar(
								R.id.ntification_progressBar, 100, persPub, false);
						mNotificationView.setTextViewText(R.id.notification_text,
								getString(R.string.dictionary_download_in_progress)
										+ " " + persPub + " %");
						mNotification.contentView = mNotificationView;
						mNotifyManager.notify(0, mNotification);
						perc = persPub;
					}
				}
			}catch(IOException ex){
				Log.w("ParserService", "ConnectionLost"+ex);

			}
			

		}
		

	}

	/**
	 * Downloads dictionaries.
	 */
	@Override
	protected void onHandleIntent(Intent arg0) {
		boolean downloadedJapDict = false;
		boolean downloadedKanjiDict = false;
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
			while(true){
				if(!connected){
					Thread.sleep(500);
				}else{
					if(downloadFile(url,outputFile)){
						//file downloaded succesfully 
						break;
					}
				}
			}

				downloadedJapDict = true;
				mNotificationView.setProgressBar(
						R.id.ntification_progressBar, 0, 0, false);
				mNotificationView.setViewVisibility(R.id.ntification_progressBar,
						View.GONE);
				mNotificationView.setTextViewText(R.id.notification_text,
						getString(R.string.dictionary_download_complete));
				mNotification.contentView = mNotificationView;

				mNotifyManager.notify(0, mNotification);
				Log.i("ParserService", "Downloading dictionary finished");


			// downloading kanjidict
			url = null;
			try {
				url = new URL(ParserService.KANJIDICT_PATH);
			} catch (MalformedURLException ex) {
				Log.e("ParserService",
						"Error: creating url for downloading kanjidict2");
			}
			if (url != null) {
			

				mNotificationView.setTextViewText(R.id.notification_title,
						getString(R.string.dictionary_kanji_download_title));
				mNotificationView.setViewVisibility(R.id.ntification_progressBar,
						View.VISIBLE);
				kanjiDictPath = karta.getPath() + File.separator
						+ "kanjidict.gz";
				File fileKanjidict = new File(kanjiDictPath);
				if(fileKanjidict.exists()){
					fileKanjidict.delete();
				}
				while(true){
					if(!connected){
						Thread.sleep(500);
					}else{
						if(downloadFile(url,fileKanjidict)){
							//file downloaded succesfully 
							break;
						}
					}
				}
				
				downloadedKanjiDict = true;

				mNotificationView.setProgressBar(
						R.id.ntification_progressBar, 0, 0, false);
				mNotificationView.setViewVisibility(R.id.ntification_progressBar,
						View.GONE);
				mNotificationView
						.setTextViewText(
								R.id.notification_text,
								getString(R.string.dictionary_download_complete));
				mNotification.contentView = mNotificationView;

				mNotifyManager.notify(0, mNotification);
				Log.i("ParserService",
						"Downloading dictionaries finished");
			}

			String japDictAbsolutePath = null;
			String japKanjiDictAbsolutePath = null;
			if (downloadedJapDict) {
				japDictAbsolutePath = parseDictionary(dictionaryPath);
			}
			if (downloadedKanjiDict) {
				japKanjiDictAbsolutePath = parseKanjiDict(kanjiDictPath);
			}
			if (japDictAbsolutePath != null) {
				serviceSuccessfullyDone(japDictAbsolutePath,
						japKanjiDictAbsolutePath);
			} else {
				Log.e("ParserService", "Parsing dictionary failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ParserService", "Exception: " + e.toString());
		}

		stopSelf();
	}

	/**
	 * Parse downloaded JMdict dictionary.
	 * 
	 * @param path
	 *            to the dictionary gziped file
	 * @return path to lucene folder for jmdict
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private String parseDictionary(String path) throws InterruptedException,
			IOException, ParserConfigurationException, SAXException {

		mNotificationView.setTextViewText(R.id.notification_text,
				getString(R.string.dictionary_parsing_in_progress));
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

			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(DICTIONARY_PREFERENCES, true);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			mNotification.contentIntent = resultPendingIntent;
			mNotifyManager.notify(0, mNotification);

			complete = true;

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
			stopSelf();
		} catch (Exception ex) {
			Log.e("ParserService",
					"SaxDataHolder - Unknown exception: " + ex.toString());
			stopSelf();
		}
		return null;

	}

	/**
	 * Parse downloaded KanjiDict2 dictionary.
	 * 
	 * @param path
	 *            to the KanjiDict2 dictionary gziped file
	 * @return path to lucene folder for kanjidict2
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private String parseKanjiDict(String path) throws InterruptedException,
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

			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(DICTIONARY_PREFERENCES, true);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			mNotification.contentIntent = resultPendingIntent;
			mNotifyManager.notify(0, mNotification);

			complete = true;

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
			stopSelf();
		} catch (Exception ex) {
			Log.e("ParserService",
					"SaxDataHolderKanjiDict - Unknown exception: "
							+ ex.toString());
			stopSelf();
		}
		return null;

	}

	/**
	 * Called when parsing was succesfully done. Sets shared preferences and
	 * broadcast downloadingDictinaryServiceDone intent.
	 * 
	 * @param dictionaryPath
	 *            path to JMdict dictionary
	 * @param kanjiDictPath
	 *            path to kanjidict2 dictionary
	 */
	private void serviceSuccessfullyDone(String dictionaryPath,
			String kanjiDictPath) {
		Log.i("ParserService",
				"Parsing dictionary - parsing succesfully done, saving preferences");
		SharedPreferences settings = getSharedPreferences(
				DICTIONARY_PREFERENCES, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("hasValidDictionary", true);
		Log.i("ParserService", "Dictionary path: " + dictionaryPath);
		Log.i("ParserService", "KanjiDict path: " + kanjiDictPath);
		editor.putString("pathToDictionary", dictionaryPath);
		editor.putBoolean("hasValidKanjiDictionary", true);
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
	}

	/**
	 * Closes IO streams. If service wasn't done succesfully changes
	 * notification and broadcasts serviceCanceled intent.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(internetReceiver);
		if (!complete) {
			mNotificationView.setTextViewText(R.id.notification_text,
					getString(R.string.dictionary_download_interrupted));
			mNotification.contentView = mNotificationView;
			mNotifyManager.notify(0, mNotification);
			Intent intent = new Intent("serviceCanceled");
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.w("ParserService", "Service ending none complete");
		}
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
	 * @param directory
	 *            directory to be deleted
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
