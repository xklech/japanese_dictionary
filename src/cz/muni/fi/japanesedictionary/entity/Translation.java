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

package cz.muni.fi.japanesedictionary.entity;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.security.MessageDigest;

import org.json.JSONArray;
import org.json.JSONException;


import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;

import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;

/**
 * Entity class for translation
 * @author Jaroslav Klech
 *
 */
public class Translation {
	public static final String SAVE_JAPANESE_KEB = "cz.muni.fi.japanesedictionary.japanese_keb";
	public static final String SAVE_JAPANESE_REB = "cz.muni.fi.japanesedictionary.japanese_reb";
	public static final String SAVE_DUTCH = "cz.muni.fi.japanesedictionary.dutch";
	public static final String SAVE_ENGLISH = "cz.muni.fi.japanesedictionary.english";
	public static final String SAVE_FRENCH = "cz.muni.fi.japanesedictionary.french";
	public static final String SAVE_GERMAN = "cz.muni.fi.japanesedictionary.german";
	
    private static final String LOG_TAG = "Translation";
	
	private List<String> mJapKeb;
	private List<String> mJapReb;
	private List<List<String>> mEnglish;
	private List<List<String>> mFrench;
	private List<List<String>> mDutch;
	private List<List<String>> mGerman;
	
	@Override
	public String toString() {
		return "Translation [jap_keb=" + mJapKeb + ", jap_reb=" + mJapReb
				+ ", english=" + mEnglish + ", french=" + mFrench + ", dutch="
				+ mDutch + ", german=" + mGerman + "]";
	}

	public Translation(){
		mJapKeb = new ArrayList<String>();
		mJapReb = new ArrayList<String>();
		mEnglish = new ArrayList<List<String>>();
		mFrench = new ArrayList<List<String>>();
		mDutch = new ArrayList<List<String>>();
		mGerman = new ArrayList<List<String>>();
	}
	
	public void addJapKeb(String keb){
		if(keb == null || keb.length() < 1){
			return ;
		}
		mJapKeb.add(keb);
	}
	
	public void addJapReb(String reb){
		if(reb == null || reb.length() < 1){
			return ;
		}
		mJapReb.add(reb);
	}
	
	public void addEnglishSense(List<String> sense){
		if(sense == null || sense.size() < 1){
			return ;
		}
		mEnglish.add(sense);
	}
	public void addFrenchSense(List<String> sense){
		if(sense == null || sense.size() < 1){
			return ;
		}
		mFrench.add(sense);
	}
	public void addDutchSense(List<String> sense){
		if(sense == null || sense.size() < 1){
			return ;
		}
		mDutch.add(sense);
	}
	public void addGermanSense(List<String> sense){
		if(sense == null || sense.size() < 1){
			return ;
		}
		mGerman.add(sense);
	}
	
	public List<String> getJapaneseKeb(){
		return mJapKeb.isEmpty()?null:mJapKeb;
	}
	
	public List<String> getJapaneseReb(){
		return mJapReb.isEmpty()?null:mJapReb;
	}
	
	public List<List<String>> getEnglishSense(){
		return mEnglish.isEmpty()?null:mEnglish;
	}
	
	public List<List<String>> getFrenchSense(){
		return mFrench.isEmpty()?null:mFrench;
	}
	
	public List<List<String>> getDutchSense(){
		return mDutch.isEmpty()?null:mDutch;
	}
	
	public List<List<String>> getGermanSense(){
		return mGerman.isEmpty()?null:mGerman;
	}
	
	/**
	 * Takes json string and parses it list of Japanese Keb.
	 * 
	 * @param jsonString - JSON string to be parsed
	 */
	public void parseJapaneseKeb(String jsonString){
    	if(jsonString != null){
    		List<String> japKeb;
        	JSONArray language_senses;
    		try {
    			language_senses = new JSONArray(jsonString);
    		} catch (JSONException e) {
    			Log.w(LOG_TAG,"getting parseJapaneseKeb()  initial expression failed: "+ e.toString());
    			return ;
    		}
    		japKeb = this.parseOneSense(language_senses);
	    	for(String str: japKeb){
	    		this.addJapKeb(str);
	    	}
    	}
	}

	/**
	 * Takes json string and parses it list of Japanese Reb.
	 * 
	 * @param jsonString - JSON string to be parsed
	 */
	public void parseJapaneseReb(String jsonString){
    	if(jsonString != null){
    		List<String> japReb;
        	JSONArray language_senses;
    		try {
    			language_senses = new JSONArray(jsonString);
    		} catch (JSONException e) {
    			Log.w(LOG_TAG,"getting parseJapaneseReb()  initial expression failed: "+ e.toString());
    			return ;
    		}
    		japReb = this.parseOneSense(language_senses);
	    	for(String str: japReb){
	    		this.addJapReb(str);
	    	}
    	}
	}	

	/**
	 * Universal private function for parsing one JSONArray sense
	 * 
	 * @param sense  JSONArray which should be parset to sense
	 * @return List<String> returns list of parsed senses
	 */
	private List<String> parseOneSense(JSONArray sense){
		if(sense == null){
			return null;
		}
		List<String> senseTranslation = new ArrayList<String>();
		for(int k = 0; k < sense.length();k++  ){
			String oneSense;
			try {
				oneSense = sense.getString(k);
				senseTranslation.add(oneSense);
			} catch (JSONException e) {
				Log.w(LOG_TAG,"getting parseOneSense() expression failed: "+ e.toString());
				e.printStackTrace();
			}
		}
		return senseTranslation;
	}
	
	/**
	 * Takes json string and parses it list of English meanings.
	 * 
	 * @param jsonString - JSON string to be parsed
	 */
	public void parseEnglish(String jsonString){
    	if(jsonString == null){
    		return ;
    	}
    	JSONArray language_senses;
		try {
			language_senses = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w(LOG_TAG,"getting parseEnglish()  initial expression failed: "+ e.toString());
			return ;
		}
	
    	for( int j = 0; j< language_senses.length(); j++){
			if(!language_senses.isNull(j)){
				List<String> sense;
				try {
					sense = parseOneSense(language_senses.getJSONArray(j));
					this.addEnglishSense(sense);
				} catch (JSONException e) {
					Log.w(LOG_TAG,"getting parseEnglish() expression failed: "+ e.toString());
				}
			}
    	}
	}


	/**
	 * Takes json string and parses it list of Dutch meanings.
	 * 
	 * @param jsonString - JSON string to be parsed
	 */
	public void parseDutch(String jsonString){
    	if(jsonString == null){
    		return ;
    	}
    	JSONArray language_senses;
		try {
			language_senses = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w(LOG_TAG,"getting parseDutch()  initial expression failed: "+ e.toString());
			return ;
		}
	
    	for( int j = 0; j< language_senses.length(); j++){
			if(!language_senses.isNull(j)){
				List<String> sense;
				try {
					sense = parseOneSense(language_senses.getJSONArray(j));
					this.addDutchSense(sense);
				} catch (JSONException e) {
					Log.w(LOG_TAG,"getting parseDutch() expression failed: "+ e.toString());
				}
			}
    	}
	}	

	/**
	 * Takes json string and parses it list of French meanings.
	 * 
	 * @param jsonString - JSON string to be parsed
	 */
	public void parseFrench(String jsonString){
    	if(jsonString == null){
    		return ;
    	}
    	JSONArray language_senses;
		try {
			language_senses = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w(LOG_TAG,"getting parseFrench()  initial expression failed: "+ e.toString());
			return ;
		}
	
    	for( int j = 0; j< language_senses.length(); j++){
			if(!language_senses.isNull(j)){
				List<String> sense;
				try {
					sense = parseOneSense(language_senses.getJSONArray(j));
					this.addFrenchSense(sense);
				} catch (JSONException e) {
					Log.w(LOG_TAG,"getting parseFrench() expression failed: "+ e.toString());
				}
			}
    	}
	}	

	/**
	 * Takes json string and parses it list of German meanings.
	 * 
	 * @param jsonString - JSON string to be parsed
	 */	
	public void parseGerman(String jsonString){
    	if(jsonString == null){
    		return ;
    	}
    	JSONArray language_senses;
		try {
			language_senses = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w(LOG_TAG,"getting parseGerman()  initial expression failed: "+ e.toString());
			return ;
		}
	
    	for( int j = 0; j< language_senses.length(); j++){
			if(!language_senses.isNull(j)){
				List<String> sense;
				try {
					sense = parseOneSense(language_senses.getJSONArray(j));
					this.addGermanSense(sense);
				} catch (JSONException e) {
					Log.w(LOG_TAG,"getting parseGerman() expression failed: "+ e.toString());
				}
			}
    	}
	}
	
	/**
	 *  Adds Translation to given bundle.
	 * 
	 * @param bundle - bundle in which translation should be saved
	 * 				 - in case of null empty bundle is created
	 * @return Bundle returns bundle which contains Translation
	 */
	public Bundle createBundleFromTranslation(Bundle bundle){
		if(bundle == null){
			bundle = new Bundle();
		}
		if(this.getJapaneseKeb() != null && this.getJapaneseKeb().size() > 0){
			bundle.putString(SAVE_JAPANESE_KEB, (new JSONArray(this.getJapaneseKeb())).toString());
		}
		if(this.getJapaneseReb() != null && this.getJapaneseReb().size() > 0){
			bundle.putString(SAVE_JAPANESE_REB, (new JSONArray(this.getJapaneseReb())).toString());
		}
		if(this.getDutchSense() != null && this.getDutchSense().size() > 0){	
			JSONArray sense = convertToJSON(this.getDutchSense());
			bundle.putString(SAVE_DUTCH,sense.toString());
		}
		
		if(this.getEnglishSense() != null && this.getEnglishSense().size() > 0){	
			JSONArray sense = convertToJSON(this.getEnglishSense());
			bundle.putString(SAVE_ENGLISH, sense.toString());
		}
		if(this.getFrenchSense() != null && this.getFrenchSense().size() > 0){	
			JSONArray sense = convertToJSON(this.getFrenchSense());
			bundle.putString(SAVE_FRENCH, sense.toString());
		}
		if(this.getGermanSense() != null && this.getGermanSense().size() > 0){
			JSONArray sense = convertToJSON(this.getGermanSense());
			bundle.putString(SAVE_GERMAN, sense.toString());
		}
		return bundle;
	}

	/**
	 * Creates ContentValues from translations for purpose of saving in database.
	 * 
	 * @return ContentValues contains saved Translation
	 */
	public ContentValues createContentValuesFromTranslation(){
		ContentValues values = new ContentValues();
		if(this.getJapaneseKeb() != null && this.getJapaneseKeb().size() > 0){
			values.put(GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_KEB, (new JSONArray(this.getJapaneseKeb())).toString());
		}
		if(this.getJapaneseReb() != null && this.getJapaneseReb().size() > 0){
			values.put(GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_REB, (new JSONArray(this.getJapaneseReb())).toString());
		}
		if(this.getDutchSense() != null && this.getDutchSense().size() > 0){	
			JSONArray sense = convertToJSON(this.getDutchSense());
			values.put(GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_DUTCH,sense.toString());
		}
		
		if(this.getEnglishSense() != null && this.getEnglishSense().size() > 0){	
			JSONArray sense = convertToJSON(this.getEnglishSense());
			values.put(GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_ENGLISH, sense.toString());
		}
		if(this.getFrenchSense() != null && this.getFrenchSense().size() > 0){	
			JSONArray sense = convertToJSON(this.getFrenchSense());
			values.put(GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_FRENCH, sense.toString());
		}
		if(this.getGermanSense() != null && this.getGermanSense().size() > 0){
			JSONArray sense = convertToJSON(this.getGermanSense());
			values.put(GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_GERMAN, sense.toString());
		}
        values.put(GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_LAST_VIEWED, (new Date()).getTime());
		return values;
	}
	
	/**
	 *	Creates new instance of Translation from saved instance in given bundle.
	 * 
	 * @param bundle bundle with saved Translation
	 * @return returns new instance of Translation or null if bundle is null
	 */
	public static Translation newInstanceFromBundle(Bundle bundle){
		if(bundle == null){
			return null;
		}
		Translation translation = new Translation();
		
    	String japaneseKeb = bundle.getString(SAVE_JAPANESE_KEB);
    	String japaneseReb = bundle.getString(SAVE_JAPANESE_REB);
    	String english = bundle.getString(SAVE_ENGLISH);
    	String french = bundle.getString(SAVE_FRENCH);
    	String dutch = bundle.getString(SAVE_DUTCH);
    	String german = bundle.getString(SAVE_GERMAN);   
    	
    	translation.parseJapaneseKeb(japaneseKeb);
    	translation.parseJapaneseReb(japaneseReb);
    	translation.parseEnglish(english);
    	translation.parseFrench(french);
    	translation.parseDutch(dutch);
    	translation.parseGerman(german);
    	
		return translation.getJapaneseReb()!=null && translation.getJapaneseReb().size() > 0 ? translation : null;
	}
	
	
	/**
	 * Takes list of senses and convert it to one JSONArray
	 * 
	 * @param senses - List of senses
	 * @return coverted JSONArray
	 */
	
	private JSONArray convertToJSON(List<List<String>> senses){
		JSONArray senseJSON = new JSONArray();
		for(List<String> oneSense : senses){
			JSONArray innerJSON = new JSONArray(oneSense);
			senseJSON.put(innerJSON);
		}
		return senseJSON;
		
	}

    public String getIndexHash(){
        StringBuilder hashString = new StringBuilder();
        hashString.append(getJapaneseKeb()).append(getJapaneseReb());
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = messageDigest.digest(hashString.toString().getBytes());
            return new BigInteger(1,hashBytes).toString(16);
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG,"getting getIndexHash() hash failed: "+ e.toString());
        }
        return null;
    }
}
