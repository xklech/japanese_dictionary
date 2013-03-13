package cz.muni.japanesedictionary.entity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class Translation {
	private List<String> jap_keb;
	private List<String> jap_reb;
	private List<List<String>> english;
	private List<List<String>> french;
	private List<List<String>> dutch;
	private List<List<String>> german;
	
	@Override
	public String toString() {
		return "Translation [jap_keb=" + jap_keb + ", jap_reb=" + jap_reb
				+ ", english=" + english + ", french=" + french + ", dutch="
				+ dutch + ", german=" + german + "]";
	}

	public Translation(){
		jap_keb = new ArrayList<String>();
		jap_reb = new ArrayList<String>();
		english = new ArrayList<List<String>>();
		french = new ArrayList<List<String>>();
		dutch = new ArrayList<List<String>>();
		german = new ArrayList<List<String>>();
	}
	
	public void addJapKeb(String keb){
		if(keb == null || keb.length() < 1){
			return ;
		}
		jap_keb.add(keb);
	}
	
	public void addJapReb(String reb){
		if(reb == null || reb.length() < 1){
			return ;
		}
		jap_reb.add(reb);
	}
	
	public void addEnglishSense(List<String> sense){
		if(sense == null || sense.size() < 1){
			return ;
		}
		english.add(sense);
	}
	public void addFrenchSense(List<String> sense){
		if(sense == null || sense.size() < 1){
			return ;
		}
		french.add(sense);
	}
	public void addDutchSense(List<String> sense){
		if(sense == null || sense.size() < 1){
			return ;
		}
		dutch.add(sense);
	}
	public void addGermanSense(List<String> sense){
		if(sense == null || sense.size() < 1){
			return ;
		}
		german.add(sense);
	}
	
	public List<String> getJapaneseKeb(){
		return jap_keb.isEmpty()?null:jap_keb;
	}
	
	public List<String> getJapaneseReb(){
		return jap_reb.isEmpty()?null:jap_reb;
	}
	
	public List<List<String>> getEnglishSense(){
		return english.isEmpty()?null:english;
	}
	
	public List<List<String>> getFrenchSense(){
		return french.isEmpty()?null:french;
	}
	
	public List<List<String>> getDutchSense(){
		return dutch.isEmpty()?null:dutch;
	}
	
	public List<List<String>> getGermanSense(){
		return german.isEmpty()?null:german;
	}
	
	public void parseJapaneseKeb(String jsonString){
    	if(jsonString != null){
    		List<String> japKeb = null;
        	JSONArray language_senses;
    		try {
    			language_senses = new JSONArray(jsonString);
    		} catch (JSONException e) {
    			Log.w("Translation","getting parseJapaneseKeb()  initial expression failed: "+ e.toString());
    			return ;
    		}
    		japKeb = this.parseOneSense(language_senses);
	    	for(String str: japKeb){
	    		this.addJapKeb(str);
	    	}
    	}
	}
	
	public void parseJapaneseReb(String jsonString){
    	if(jsonString != null){
    		List<String> japReb = null;
        	JSONArray language_senses;
    		try {
    			language_senses = new JSONArray(jsonString);
    		} catch (JSONException e) {
    			Log.w("Translation","getting parseJapaneseReb()  initial expression failed: "+ e.toString());
    			return ;
    		}
    		japReb = this.parseOneSense(language_senses);
	    	for(String str: japReb){
	    		this.addJapReb(str);
	    	}
    	}
	}	
	
	private List<String> parseOneSense(JSONArray sense){
		if(sense == null){
			return null;
		}
		List<String> senseTranslation = new ArrayList<String>();
		for(int k = 0; k < sense.length();k++  ){
			String oneSense = null;
			try {
				oneSense = sense.getString(k);
				senseTranslation.add(oneSense);
			} catch (JSONException e) {
				Log.w("Translation","getting parseOneSense() expression failed: "+ e.toString());
				e.printStackTrace();
			}
		}
		return senseTranslation;
	}
	
	public void parseEnglish(String jsonString){
    	if(jsonString == null){
    		return ;
    	}
    	JSONArray language_senses;
		try {
			language_senses = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("Translation","getting parseEnglish()  initial expression failed: "+ e.toString());
			return ;
		}
	
    	for( int j = 0; j< language_senses.length(); j++){
			if(!language_senses.isNull(j)){
				List<String> sense;
				try {
					sense = parseOneSense(language_senses.getJSONArray(j));
					this.addEnglishSense(sense);
				} catch (JSONException e) {
					Log.w("Translation","getting parseEnglish() expression failed: "+ e.toString());
				}
			}
    	}
	}
	
	public void parseDutch(String jsonString){
    	if(jsonString == null){
    		return ;
    	}
    	JSONArray language_senses;
		try {
			language_senses = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("Translation","getting parseDutch()  initial expression failed: "+ e.toString());
			return ;
		}
	
    	for( int j = 0; j< language_senses.length(); j++){
			if(!language_senses.isNull(j)){
				List<String> sense;
				try {
					sense = parseOneSense(language_senses.getJSONArray(j));
					this.addDutchSense(sense);
				} catch (JSONException e) {
					Log.w("Translation","getting parseDutch() expression failed: "+ e.toString());
				}
			}
    	}
	}	
	
	public void parseFrench(String jsonString){
    	if(jsonString == null){
    		return ;
    	}
    	JSONArray language_senses;
		try {
			language_senses = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("Translation","getting parseFrench()  initial expression failed: "+ e.toString());
			return ;
		}
	
    	for( int j = 0; j< language_senses.length(); j++){
			if(!language_senses.isNull(j)){
				List<String> sense;
				try {
					sense = parseOneSense(language_senses.getJSONArray(j));
					this.addFrenchSense(sense);
				} catch (JSONException e) {
					Log.w("Translation","getting parseFrench() expression failed: "+ e.toString());
				}
			}
    	}
	}	
		
	public void parseGerman(String jsonString){
    	if(jsonString == null){
    		return ;
    	}
    	JSONArray language_senses;
		try {
			language_senses = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("Translation","getting parseGerman()  initial expression failed: "+ e.toString());
			return ;
		}
	
    	for( int j = 0; j< language_senses.length(); j++){
			if(!language_senses.isNull(j)){
				List<String> sense;
				try {
					sense = parseOneSense(language_senses.getJSONArray(j));
					this.addGermanSense(sense);
				} catch (JSONException e) {
					Log.w("Translation","getting parseGerman() expression failed: "+ e.toString());
				}
			}
    	}
	}		
}
