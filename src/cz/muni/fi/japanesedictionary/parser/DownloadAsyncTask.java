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

import android.app.Notification;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.main.MainActivity;

public class DownloadAsyncTask extends AsyncTask<Void, Boolean, Boolean>{


	private NotificationManager mNotifyManager = null;
	private Notification mNotification = null;
	private RemoteViews mNotificationView = null;
	private boolean mAppend = false;
	private ParserService2 mService;
	
	
	private URL mDownloadJMDictFrom = null;
	private File mDownloadJMDictTo = null;
	private URL mDownloadKanjidicFrom = null;
	private File mDownloadKanjidicTo = null;
	

	
	
	
	public DownloadAsyncTask(Notification notif,RemoteViews remote,NotificationManager manager,
			boolean append,ParserService2 service){
		mNotifyManager = manager;
		mNotification = notif;
		mNotificationView = remote;
		mAppend = append;
		mService = service;
	}
	
	
	@Override
	protected Boolean doInBackground(Void... params) {



		String dictionaryPath = null;
		String kanjiDictPath = null;

		URL url = null;
		try {
			url = new URL(ParserService.DICTIONARY_PATH);
		} catch (MalformedURLException ex) {
			Log.e("ParserService",
					"Error: creating url for downloading dictionary");
			return false;
		}



		try {
			File karta = null;
			if (MainActivity.canWriteExternalStorage()) {
				// je dostupna karta
				karta = mService.getExternalCacheDir();
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

			
			// downloading kanjidict
			url = null;
			try {
				url = new URL(ParserService.KANJIDICT_PATH);
			} catch (MalformedURLException ex) {
				Log.e("ParserService","Error: creating url for downloading kanjidict2");
			}
			if (url != null) {
				kanjiDictPath = karta.getPath() + File.separator + "kanjidict.gz";
				File fileKanjidict = new File(kanjiDictPath);
				if(fileKanjidict.exists()){
					fileKanjidict.delete();
				}
				mDownloadKanjidicFrom = url;
				mDownloadKanjidicTo = fileKanjidict;
			}
			
			
			return downloadDictionaries();
			
		} catch(MalformedURLException e){
			Log.e("ParserService", "MalformedURLException wrong format of URL: " + e.toString());
		} catch(IOException e){
			Log.e("ParserService", "IOException downloading interrupted: " + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ParserService", "Exception: " + e.toString());
		}
		return false;

		
	}

	
	@Override
	protected void onPostExecute(Boolean succes) {
		if(mService!= null){
			if(succes){
				mService.downloadingSuccesfullyDone();
			}else{
				mService.setCurrentlyDownloading(false);
			}
		}
		super.onPostExecute(succes);
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
		BufferedInputStream input = null;
		OutputStream output = null;
		
		HttpURLConnection connection = null;
		
		connection = (HttpURLConnection)url.openConnection();
		connection.setRequestProperty("Accept-Encoding", "identity");
		connection.connect();
		long fileLength = connection.getContentLength();
		long total = 0;

		
		if(outputFile.exists()){
			if(fileLength == outputFile.length()){
				return true;
			}
			if(mAppend){
				output = new FileOutputStream(outputFile.getPath(), true);
				total = outputFile.length();
				connection = (HttpURLConnection)url.openConnection();
				connection.setRequestProperty("Accept-Encoding", "identity");
				connection.setRequestProperty("Range", "bytes=" + total + "-");
				connection.connect();
			}else{
				deleteDirectory(outputFile);
				output = new FileOutputStream(outputFile.getPath(), true);
			}
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
							mService.getString(R.string.dictionary_download_in_progress));
			mNotification.contentView = mNotificationView;
			mNotifyManager.notify(0, mNotification);
		}
		
		
		
		
		byte data[] = new byte[1024];

		int count = 0;
		int perc = 0;
		long lastUpdate = (new Date()).getTime();
		try{
			while ((count = input.read(data)) != -1) {
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
									mService.getString(R.string.dictionary_download_in_progress)
											+ " " + persPub + " %");
							mNotification.contentView = mNotificationView;
							mNotifyManager.notify(0, mNotification);
							perc = persPub;
						}
					}		
			}
		}catch(IOException ex){
			Log.w("ParserService", "ConnectionLost: "+ex);
			closeIOStreams(input, output);
			return false;
		}
		closeIOStreams(input, output);
		return true;

	}

	/**
	 * Downloads dictionaries and launches parseDictionaries()
	 * 
	 * @throws IOException
	 */
	private boolean downloadDictionaries() throws IOException{

		if(downloadFile(mDownloadJMDictFrom,mDownloadJMDictTo)){
			mService.setDownloadedJmDict(mDownloadJMDictTo);
		}else{
			return false;
		}

			mNotificationView.setTextViewText(R.id.notification_title,
					mService.getString(R.string.dictionary_kanji_download_title));
			mNotificationView.setViewVisibility(R.id.ntification_progressBar,
					View.VISIBLE);
			if(downloadFile(mDownloadKanjidicFrom,mDownloadKanjidicTo)){
				mService.setDownloadedKanjidic(mDownloadKanjidicTo);				
			}else{
				return false;
			}
			
			return true;
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
		if(directory == null){
			return false;
		}
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if(files != null){
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}
	
	
	
}
