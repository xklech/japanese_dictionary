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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import cz.muni.fi.japanesedictionary.Const;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.main.MainActivity;
import cz.muni.fi.japanesedictionary.util.CompressFolder;

/**
 * Service for downloading and parsing dictionaries.
 * 
 * @author Bc. Jaroslav Klech
 * 
 */
public class ParserService extends Service {

	private static final String LOG_TAG = "ParserService";
	
	public static final String DICTIONARY_PATH = "http://android-japdict.rhcloud.com/cron/jmdict";
	public static final String KANJIDICT_PATH = "http://android-japdict.rhcloud.com/cron/kanjidic2";
    public static final String TATOEBA_INDICES_PATH = "http://android-japdict.rhcloud.com/cron/tatoebaindices";
    public static final String TATOEBA_SENTENCES_PATH = "http://android-japdict.rhcloud.com/cron/tatoebasentences";
    public static final String KANJIVG_PATH = "https://github.com/KanjiVG/kanjivg/releases/download/r20140816/kanjivg-20140816-main.zip";


	public static final String DICTIONARY_PREFERENCES = "cz.muni.fi.japanesedictionary";

    public static final Long downloadExpireTime = 60 * 60 * 3l;


	private URL mDownloadJMDictFrom = null;
	private File mDownloadJMDictTo = null;
	private boolean mDownloadingJMDict = false;

	private URL mDownloadKanjidicFrom = null;
	private File mDownloadKanjidicTo = null;
	private boolean mDownloadingKanjidic = false;

    private URL mDownloadTatoebaIndicesFrom = null;
    private File mDownloadTatoebaIndicesTo = null;
    private boolean mDownloadingTatoebaIndices = false;

    private URL mDownloadTatoebaSentencesFrom = null;
    private File mDownloadTatoebaSentencesTo = null;
    private boolean mDownloadingTatoebaSentences = false;

    private URL mDownloadKanjiVGFrom = null;
    private File mDownloadKanjiVGTo = null;
    private boolean mDownloadingKanjiVG = false;


	private boolean mDownloadInProgress = false;
	private boolean mCurrentlyDownloading = false;
	private boolean mParsing = false;

	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotifyManager = null;
	private Notification mNotification = null;
	private boolean mComplete = false;
    private boolean mNotEnoughSpace = false;


    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private boolean mRedelivery;
    private int mStartId;

    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
        	mStartId = msg.arg1;
            onHandleIntent();
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
				Log.w(LOG_TAG,"IOException caught while downloading: "+e.toString());
				stopSelf(mStartId);
			}
        }
    }

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
				Log.i(LOG_TAG,"Connection lost");
			}else{
				Log.i(LOG_TAG,"Connection Established");
				if(mDownloadInProgress && !mCurrentlyDownloading){
			        Message msg = mServiceHandler.obtainMessage();
			        mServiceHandler = new ServiceDownloadHandler(mServiceLooper);
			        mServiceHandler.sendMessage(msg);
				}
			}
		}
	};


	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
        HandlerThread thread = new HandlerThread("IntentService[Dictionary_parser]");
        thread.start();
        
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
		setIntentRedelivery(true);

		
	}

    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
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
	 * @return true if file was downloaded successfully else false
	 * @throws IOException
	 */
	private boolean downloadFile(URL url, File outputFile) 
			throws IOException
			{
		mCurrentlyDownloading = true;
		BufferedInputStream input;
		OutputStream output;
		
		HttpURLConnection connection;
                connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Accept-Encoding", "identity");
		connection.connect();
		long fileLength = connection.getContentLength();
		long total = 0;

		
		if(outputFile.exists()){
            if((outputFile.lastModified()+downloadExpireTime) < System.currentTimeMillis()){
                CompressFolder.deleteDirectory(outputFile);
                output = new FileOutputStream(outputFile.getPath());
            }else{
                output = new FileOutputStream(outputFile.getPath(), true);
                total = outputFile.length();
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("Accept-Encoding", "identity");
                connection.setRequestProperty("Range", "bytes=" + total + "-");
                connection.connect();
            }

		}else{
            output = new FileOutputStream(outputFile.getPath());
        }



		// velikost souboru
		input = new BufferedInputStream(connection.getInputStream(), 1024);

		if (fileLength == -1) {
            mBuilder.setProgress(100, 0, false)
                    .setContentTitle(getString(R.string.dictionary_download_title))
                    .setContentText(getString(R.string.dictionary_download_in_progress))
                    .setContentInfo("0%");

			mNotifyManager.notify(0, mBuilder.build());
		}
		
		
		
		
		byte data[] = new byte[1024];

		int count;
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
								
                                mBuilder.setProgress(100, persPub, false)
                                        .setContentInfo(persPub + "%");

								mNotifyManager.notify(0, mBuilder.build());
								perc = persPub;
							}
						}
					}
			}
		}catch(IOException ex){
			Log.w(LOG_TAG, "ConnectionLost: "+ex);
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
        Log.i(LOG_TAG,"downloading JMDict");
		if(mDownloadingJMDict){
			if(downloadFile(mDownloadJMDictFrom,mDownloadJMDictTo)){
				mDownloadingJMDict = false;
				mDownloadingKanjidic = true;
			}else{
				return;
			}
		}
        Log.i(LOG_TAG,"downloading Kanjidic2");
		if(mDownloadingKanjidic){
            mBuilder.setContentTitle(getString(R.string.dictionary_kanji_download_title))
                    .setProgress(100, 0, false)
                    .setContentText(getString(R.string.dictionary_download_in_progress) + " (2/5)")
                    .setContentInfo("0%");

            mNotifyManager.notify(0, mBuilder.build());

			if(downloadFile(mDownloadKanjidicFrom, mDownloadKanjidicTo)){
				mDownloadingKanjidic = false;
                mDownloadingTatoebaIndices = true;

				Log.i(LOG_TAG, "Downloading dictionary finished");
				
			}else{
				return;
			}
		}
        Log.i(LOG_TAG,"downloading Tatoeba japanese");
        if(mDownloadingTatoebaIndices){
            mBuilder.setContentTitle(getString(R.string.dictionary_tatoeba_download_title))
                    .setProgress(100, 0, false)
                    .setContentText(getString(R.string.dictionary_download_in_progress) + " (3/5)")
                    .setContentInfo("0%");

            mNotifyManager.notify(0, mBuilder.build());

            if(downloadFile(mDownloadTatoebaIndicesFrom, mDownloadTatoebaIndicesTo)){
                mDownloadingTatoebaIndices = false;
                mDownloadingTatoebaSentences = true;
                Log.i(LOG_TAG, "Downloading dictionary finished");

            }else{
                return;
            }
        }
        Log.i(LOG_TAG,"downloading Tatoeba translations");
        if(mDownloadingTatoebaSentences){
            mBuilder.setContentTitle(getString(R.string.dictionary_tatoeba_download_title))
                    .setProgress(100, 0, false)
                    .setContentText(getString(R.string.dictionary_download_in_progress) + " (4/5)")
                    .setContentInfo("0%");

            mNotifyManager.notify(0, mBuilder.build());

            if(downloadFile(mDownloadTatoebaSentencesFrom, mDownloadTatoebaSentencesTo)){
                mDownloadingTatoebaSentences = false;
                mDownloadingKanjiVG = true;

                Log.i(LOG_TAG, "Downloading dictionary finished");

            }else{
                return;
            }
        }
        Log.i(LOG_TAG,"downloading kanjivg strokes");
        if(mDownloadingKanjiVG){
            mBuilder.setContentTitle(getString(R.string.dictionary_kanjivg_download_title))
                    .setProgress(100, 0, false)
                    .setContentText(getString(R.string.dictionary_download_in_progress) + " (5/5)")
                    .setContentInfo("0%");

            mNotifyManager.notify(0, mBuilder.build());

            if(downloadFile(mDownloadKanjiVGFrom, mDownloadKanjiVGTo)){
                mDownloadingKanjiVG = false;
                mDownloadInProgress = false;

                Log.i(LOG_TAG, "Downloading dictionary finished");

            }else{
                return;
            }
        }

		if(!mDownloadInProgress && !mParsing){
			try {
				mParsing = true;
				parseDictionaries();
			} catch (ParserConfigurationException e) {
				Log.e(LOG_TAG,"ParserConfigurationException exception occured: "+e.toString());
				stopSelf(mStartId);
			} catch (SAXException e) {
				Log.e(LOG_TAG,"SAXException exception occured: "+e.toString());
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

        mBuilder.setContentTitle(getString(R.string.parsing_downloaded_dictionary))
                .setContentText(getString(R.string.dictionary_parsing_in_progress))
                .setProgress(100, 100, false);

        mNotifyManager.notify(0, mBuilder.build());

		String japDictAbsolutePath = parseDictionary(mDownloadJMDictTo.getPath(), "jmdict");
		String japKanjiDictAbsolutePath = parseDictionary(mDownloadKanjidicTo.getPath(), "kanjidic");
        String tatoebaIndicesAbsolutePath = parseDictionary(mDownloadTatoebaIndicesTo.getPath(), "tatoeba_japanese");
        String tatoebaSentencesAbsolutePath = parseDictionary(mDownloadTatoebaSentencesTo.getPath(), "tatoeba_translations");
        String kanjiVGAbsolutePath = parseDictionary(mDownloadKanjiVGTo.getPath(), "kanjivg");

        Log.w(LOG_TAG, "restarting notificatiomn, setting ongoing false");
		mBuilder.setAutoCancel(true)
		        .setOngoing(false);

		mNotifyManager.notify(0, mBuilder.build());
		if (japDictAbsolutePath != null) {
            mComplete = true;
			serviceSuccessfullyDone(japDictAbsolutePath, japKanjiDictAbsolutePath, tatoebaIndicesAbsolutePath, tatoebaSentencesAbsolutePath, kanjiVGAbsolutePath);
		} else {
			Log.e(LOG_TAG, "Parsing dictionary failed");
			stopSelf(mStartId);
		}
	}
	
	/**
	 * Downloads dictionaries.
	 */

	protected void onHandleIntent() {
		
		Log.i(LOG_TAG, "Creating parser service");

		Intent resultIntent = new Intent(getApplicationContext(),
				MainActivity.class);
		resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle(getString(R.string.dictionary_download_title))
            .setContentText(getString(R.string.dictionary_download_in_progress) + " (1/5)")
            .setSmallIcon(R.drawable.ic_notification)
            .setProgress(100, 0, false)
            .setContentInfo("0%")
		    .setContentIntent(resultPendingIntent);
		
		startForeground(0,mNotification);
		mNotifyManager.notify(0, mBuilder.build());
        File storage;
        if (MainActivity.canWriteExternalStorage()) {
            // external storage available
            storage = getExternalCacheDir();
        }else{
            storage = getCacheDir();
        }
        if (storage == null) {
            throw new IllegalStateException(
                    "External storage isn't accessible");
        }
        // free sapce controll
        StatFs stat = new StatFs(storage.getPath());
        long bytesAvailable;
        if(Build.VERSION.SDK_INT < 18){
            bytesAvailable = (long)stat.getBlockSize() *(long)stat.getAvailableBlocks();
        }else{
            bytesAvailable = stat.getAvailableBytes();
        }
        long megAvailable = bytesAvailable / 1048576;
        Log.d(LOG_TAG, "Megs free :" + megAvailable);
        if(megAvailable < 140){
            mInternetReceiver = null;
            mNotEnoughSpace = true;
            stopSelf(mStartId);
            return;
        }


		this.registerReceiver(mInternetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean english = sharedPrefs.getBoolean("language_english", false);
		boolean french = sharedPrefs.getBoolean("language_french", false);
		boolean dutch = sharedPrefs.getBoolean("language_dutch", false);
		boolean german = sharedPrefs.getBoolean("language_german", false);
        boolean russian = sharedPrefs.getBoolean("language_russian", false);
		if (!english && !french && !dutch && !german && !russian) {
			Log.i(LOG_TAG,
					"Setting english as only translation language");
			SharedPreferences.Editor editor_lang = sharedPrefs.edit();
			editor_lang.putBoolean("language_english", true);
			editor_lang.commit();
		}

		String dictionaryPath;
		String kanjiDictPath;

		URL url;
		try {
			url = new URL(ParserService.DICTIONARY_PATH);
		} catch (MalformedURLException ex) {
			Log.e(LOG_TAG,
					"Error: creating url for downloading dictionary");
			return;
		}



		try {

			dictionaryPath = storage.getPath() + File.separator + "dictionary.zip";
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
				Log.e(LOG_TAG,
						"Error: creating url for downloading kanjidict2");
			}
			if (url != null) {
				kanjiDictPath = storage.getPath() + File.separator
						+ "kanjidict.zip";
				File fileKanjidict = new File(kanjiDictPath);
				if(fileKanjidict.exists()){
					fileKanjidict.delete();
				}
				mDownloadingKanjidic = false;
				mDownloadKanjidicFrom = url;
				mDownloadKanjidicTo = fileKanjidict;
			}
			
			mDownloadTatoebaIndicesFrom = new URL(ParserService.TATOEBA_INDICES_PATH);
            mDownloadTatoebaIndicesTo = new File(storage, "tatoeba-japanese.zip");
            if(mDownloadTatoebaIndicesTo.exists()){
                mDownloadTatoebaIndicesTo.delete();
            }

            mDownloadTatoebaSentencesFrom = new URL(ParserService.TATOEBA_SENTENCES_PATH);
            mDownloadTatoebaSentencesTo = new File(storage, "tatoeba-translation.zip");
            if(mDownloadTatoebaSentencesTo.exists()){
                mDownloadTatoebaSentencesTo.delete();
            }

            mDownloadKanjiVGFrom = new URL(ParserService.KANJIVG_PATH);
            mDownloadKanjiVGTo = new File(storage, "kanjivg.zip");
            if(mDownloadKanjiVGTo.exists()){
                mDownloadKanjiVGTo.delete();
            }



			downloadDictionaries();
			
		} catch(MalformedURLException e){
			Log.e(LOG_TAG, "MalformedURLException wrong format of URL: " + e.toString());
			stopSelf(mStartId);
		} catch(IOException e){
			Log.e(LOG_TAG, "IOException downloading interrupted: " + e.toString());
			stopSelf(mStartId);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Exception: " + e.toString());
			stopSelf(mStartId);
		}

		
	}

	/**
	 * Parse downloaded JMdict dictionary.
	 * 
	 * @param pathDownloaded to the dictionary gziped file
     * @param dictionaryName dictionary name
	 * @return path to lucene folder for jmdict
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private String parseDictionary(String pathDownloaded, String dictionaryName) throws
			IOException, ParserConfigurationException, SAXException {



		Log.i(LOG_TAG, "Parsing dictionary - start");

		File downloadedFile = new File(pathDownloaded);
        File externalDir = getExternalCacheDir() == null ? getCacheDir() : getExternalCacheDir();
        String indexFile = externalDir.getAbsolutePath() + File.separator + dictionaryName;
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

        CompressFolder.unzip(downloadedFile,file);
        downloadedFile.delete();
        if (renameFolder) {
            Log.i(LOG_TAG, "Parsing dictionary - rename folders");
            File directory = new File(indexFile);
            deleteDirectory(directory);
            if (file.renameTo(directory)) {
                Log.i(LOG_TAG,
                        "Parsing dictionary - folder renamed");
                file = directory;
            }
        }
        return file.getAbsolutePath();

	}



	/**
	 * Called when parsing was succesfully done. Sets shared preferences and
	 * broadcast downloadingDictinaryServiceDone intent.
	 * 
	 * @param jmdictPath path to JMdict dictionary
	 * @param kanjiDictPath path to kanjidict2 dictionary
	 */
	private void serviceSuccessfullyDone(String jmdictPath, String kanjiDictPath, String tatoebaIndicesPath, String tatoebaSentencesPath, String kanjiVgPath) {
		Log.i(LOG_TAG,
				"Parsing dictionary - parsing succesfully done, saving preferences");
		SharedPreferences settings = getSharedPreferences(
				DICTIONARY_PREFERENCES, 0);
		SharedPreferences.Editor editor = settings.edit();
		Log.i(LOG_TAG, "Dictionary path: " + jmdictPath);
		Log.i(LOG_TAG, "KanjiDict path: " + kanjiDictPath);
        Log.i(LOG_TAG, "tatoeba indices  path: " + tatoebaIndicesPath);
        Log.i(LOG_TAG, "tatoeba sentences path: " + tatoebaSentencesPath);
        Log.i(LOG_TAG, "kanjivg path: " + kanjiVgPath);
		editor.putString(Const.PREF_JMDICT_PATH, jmdictPath);
		editor.putString(Const.PREF_KANJIDIC_PATH, kanjiDictPath);
        editor.putString(Const.PREF_TATOEBA_INDICES_PATH, tatoebaIndicesPath);
        editor.putString(Const.PREF_TATOEBA_SENTENCES_PATH, tatoebaSentencesPath);
        editor.putString(Const.PREF_KANJIVG_PATH, kanjiVgPath + "/kanji");
		Date date = new Date();
		editor.putLong("dictionaryLastUpdate", date.getTime());
		editor.commit();

		Log.i(LOG_TAG, "Parsing dictionary - preferences saved");
		Intent intent = new Intent("downloadingDictinaryServiceDone");
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		stopSelf(mStartId);
	}

	/**
	 * If service wasn't done succesfully changes
	 * notification and broadcasts serviceCanceled intent.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopForeground(true);
		if(mInternetReceiver!= null){
			this.unregisterReceiver(mInternetReceiver);
		}
        mBuilder.setAutoCancel(true)
                .setOngoing(false)
                .setProgress(0, 0, false)
                .setContentText("")
                .setContentInfo("");
		if (!mComplete) {
			Log.w(LOG_TAG, "restarting notificatiomn, setting ongoing false");
            mBuilder.setContentTitle(getString(R.string.dictionary_download_interrupted));

			Intent intent = new Intent("serviceCanceled");
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			Log.w(LOG_TAG, "Service ending none complete");
		}else{
            mBuilder.setContentTitle(getString(R.string.dictionary_download_complete));
            mBuilder.setContentText(getString(R.string.dictionary_download_complete_text));
        }
        if(mNotEnoughSpace) {
            mBuilder.setContentTitle(getString(R.string.dictionary_download_interrupted));
            mBuilder.setContentText(getString(R.string.dictionary_not_enough_space));
        }
        mNotifyManager.notify(0, mBuilder.build());
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
                Log.w(LOG_TAG, "Closing input causded excaption");
			}
		}
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
                Log.w(LOG_TAG, "Closing output causded excaption");
			}
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
            for(File file: files){
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
		}
		return (directory.delete());
	}

}
