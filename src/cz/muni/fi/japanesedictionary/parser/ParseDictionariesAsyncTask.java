package cz.muni.fi.japanesedictionary.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.main.MainActivity;

public class ParseDictionariesAsyncTask extends AsyncTask<String, Void, Boolean>{
	

	private NotificationManager mNotifyManager = null;
	private Notification mNotification = null;
	private RemoteViews mNotificationView = null;
	private ParserService2 mService;
	
	String japDictAbsolutePath = null;
	String japKanjiDictAbsolutePath = null;
	
	
	
	public ParseDictionariesAsyncTask(Notification notif,RemoteViews remote,NotificationManager manager,
			ParserService2 service){
		mNotifyManager = manager;
		mNotification = notif;
		mNotificationView = remote;
		mService = service;
	}
	@Override
	protected Boolean doInBackground(String... params) {
		if(params.length != 2){
			return false;
		}
		String pathToJMDict = params[0];
		String pathToKanjiDict = params[1];

		try {
			japDictAbsolutePath = parseDictionary(pathToJMDict);
			japKanjiDictAbsolutePath = parseKanjiDict(pathToKanjiDict);
			return true;
		} catch (IOException e) {
			Log.e("ParseDictionariesAsyncTask","IOException while aprsing dictionaries: "+e);
			mService.stopSelf();
		} catch (ParserConfigurationException e) {
			Log.e("ParseDictionariesAsyncTask","ParserConfigurationException while aprsing dictionaries: "+e);
			mService.stopSelf();
		} catch (SAXException e) {
			Log.e("ParseDictionariesAsyncTask","SAXException while aprsing dictionaries: "+e);
			mService.stopSelf();;
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(result){
			mService.setComplete(true);
			mService.serviceSuccessfullyDone(japDictAbsolutePath, japKanjiDictAbsolutePath);
		}else{
			mService.setComplete(false);
			mService.stopSelf();
		}
		super.onPostExecute(result);
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
				mService.getString(R.string.dictionary_parsing_in_progress));
		mNotificationView.setViewVisibility(R.id.ntification_progressBar,
				View.VISIBLE);
		mNotificationView.setTextViewText(R.id.notification_title,
				mService.getString(R.string.parsing_downloaded_dictionary));
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

		String indexFile = mService.getExternalCacheDir().getAbsolutePath()
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
				mService.getApplicationContext(), mNotifyManager, mNotification,
				mNotificationView);

		try {
			saxParser.parse(is, handler);
			Log.i("ParserService", "Parsing dictionary - SAX ended");
			downloadedFile.delete();
			Log.i("ParserService",
					"Parsing dictionary - downloaded file deleted");
			mNotificationView.setTextViewText(R.id.notification_text,
					mService.getString(R.string.dictionary_download_complete));
			mNotificationView.setProgressBar(R.id.ntification_progressBar, 0,
					0, true);
			mNotification.contentView = mNotificationView;
			mNotifyManager.notify(0, mNotification);

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
			mService.stopSelf();
		} catch (Exception ex) {
			Log.e("ParserService",
					"SaxDataHolder - Unknown exception: " + ex.toString());
			mService.stopSelf();
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
				mService.getString(R.string.dictionary_parsing_in_progress));
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

		String indexFile = mService.getExternalCacheDir().getAbsolutePath()
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
				mService.getApplicationContext(), mNotifyManager, mNotification,
				mNotificationView);

		try {
			saxParser.parse(is, handler);
			Log.i("ParserService", "Parsing KanjiDict - SAX ended");
			downloadedFile.delete();
			Log.i("ParserService",
					"Parsing KanjiDict - downloaded file deleted");
			mNotificationView.setTextViewText(R.id.notification_text,
					mService.getString(R.string.dictionary_download_complete));
			mNotificationView.setProgressBar(R.id.ntification_progressBar, 0,
					0, true);
			mNotificationView.setViewVisibility(R.id.ntification_progressBar,
					View.GONE);
			mNotification.contentView = mNotificationView;

			Intent intent = new Intent(mService.getApplicationContext(),
					MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(ParserService2.DICTIONARY_PREFERENCES, true);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					mService.getApplicationContext(), 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			mNotification.contentIntent = resultPendingIntent;
			mNotifyManager.notify(0, mNotification);


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
			mService.stopSelf();
		} catch (Exception ex) {
			Log.e("ParserService",
					"SaxDataHolderKanjiDict - Unknown exception: "
							+ ex.toString());
			mService.stopSelf();
		}
		return null;

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
