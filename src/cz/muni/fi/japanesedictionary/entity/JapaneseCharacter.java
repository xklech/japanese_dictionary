package cz.muni.fi.japanesedictionary.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class JapaneseCharacter {

	public static final String SAVE_CHARACTER_LITERAL = "cz.muni.fi.japanesedictionary.japanesecharacter.literal";
	public static final String SAVE_CHARACTER_RADICAL = "cz.muni.fi.japanesedictionary.japanesecharacter.radical";
	public static final String SAVE_CHARACTER_GRADE = "cz.muni.fi.japanesedictionary.japanesecharacter.grade";
	public static final String SAVE_CHARACTER_STROKE_COUNT = "cz.muni.fi.japanesedictionary.japanesecharacter.stroke";
	public static final String SAVE_CHARACTER_SKIP = "cz.muni.fi.japanesedictionary.japanesecharacter.skip";
	public static final String SAVE_CHARACTER_DIC_REF = "cz.muni.fi.japanesedictionary.japanesecharacter.dicref";
	public static final String SAVE_CHARACTER_JA_ON = "cz.muni.fi.japanesedictionary.japanesecharacter.rmgroupjaon";
	public static final String SAVE_CHARACTER_JA_KUN = "cz.muni.fi.japanesedictionary.japanesecharacter.rmgroupjakun";
	public static final String SAVE_CHARACTER_ENGLISH = "cz.muni.fi.japanesedictionary.japanesecharacter.english";
	public static final String SAVE_CHARACTER_FRENCH = "cz.muni.fi.japanesedictionary.japanesecharacter.french";
	public static final String SAVE_CHARACTER_DUTCH = "cz.muni.fi.japanesedictionary.japanesecharacter.dutch";
	public static final String SAVE_CHARACTER_GERMAN = "cz.muni.fi.japanesedictionary.japanesecharacter.german";
	public static final String SAVE_CHARACTER_NANORI = "cz.muni.fi.japanesedictionary.japanesecharacter.nanori";

	
	private String literal;
	private int radicalClassic;
	private int grade;
	private int strokeCount;
	private String skip;
	private Map<String, String> dicRef;
	private List<String> rmGroupJaOn;
	private List<String> rmGroupJaKun;
	private List<String> meaningEnglish;
	private List<String> meaningFrench;
	/*
	 *  dutch and german aren't in current kanjidict 2
	 */
	private List<String> meaningDutch;
	private List<String> meaningGerman;
	
	private List<String> nanori;
	
	@Override
	public String toString() {
		return "JapaneseCharacter [literal=" + literal + ", radicalClassic="
				+ radicalClassic + ", grade=" + grade + ", strokeCount="
				+ strokeCount + ", dicRef=" + dicRef + ", rmGroupJaOn="
				+ rmGroupJaOn + ", rmGroupJaKun=" + rmGroupJaKun
				+ ", meaningEnglish=" + meaningEnglish + ", meaningFrench="
				+ meaningFrench + ", meaningDutch=" + meaningDutch
				+ ", meaningGerman=" + meaningGerman + ", nanori=" + nanori
				+ "]";
	}
	
	public JapaneseCharacter(){
		literal = null;
		radicalClassic = 0;
		grade = 0;
		strokeCount = 0;
		dicRef = new HashMap<String,String>();
		rmGroupJaOn = new ArrayList<String>();
		rmGroupJaKun = new ArrayList<String>();
		meaningEnglish = new ArrayList<String>();
		meaningFrench = new ArrayList<String>();
		meaningDutch = new ArrayList<String>();
		meaningGerman = new ArrayList<String>();
		nanori = new ArrayList<String>();
	}

	public void setLiteral(String value){
		this.literal = value;
	}
	
	public void setRadicalClassic(int value){
		this.radicalClassic = value;
	}
	
	public void setGrade(int value){
		this.grade = value;
	}
	
	public void setStrokeCount(int value){
		this.strokeCount = value;
	}

	public void setSkip(String skip) {
		this.skip = skip;
	}

	public void addDicRef(String key,String value){
		if(key == null || key.length() < 1 || value == null || value.length() < 1){
			return ;
		}
		dicRef.put(key, value);
	}
	
	public void addRmGroupJaOn(String value){
		if(value == null || value.length() < 1){
			return;
		}
		rmGroupJaOn.add(value);
	}
	
	public void addRmGroupJaKun(String value){
		if(value == null || value.length() < 1){
			return;
		}
		rmGroupJaKun.add(value);
	}
	
	public void addMeaningEnglish(String value){
		if(value == null || value.length() < 1){
			return;
		}
		meaningEnglish.add(value);
	}
	
	public void addMeaningFrench(String value){
		if(value == null || value.length() < 1){
			return;
		}
		meaningFrench.add(value);
	}	
	
	public void addMeaningDutch(String value){
		if(value == null || value.length() < 1){
			return;
		}
		meaningDutch.add(value);
	}	

	public void addMeaningGerman(String value){
		if(value == null || value.length() < 1){
			return;
		}
		meaningGerman.add(value);
	}	
	
	public void addNanori(String value){
		if(value == null || value.length() < 1){
			return;
		}
		nanori.add(value);
	}
	
	public String getLiteral() {
		return literal;
	}

	public int getRadicalClassic() {
		return radicalClassic;
	}

	public int getGrade() {
		return grade;
	}

	public int getStrokeCount() {
		return strokeCount;
	}

	public String getSkip() {
		return skip;
	}
	
	public Map<String, String> getDicRef() {
		return dicRef.size() < 1? null : dicRef;
	}

	public List<String> getRmGroupJaOn() {
		return rmGroupJaOn.size() < 1? null : rmGroupJaOn;
	}

	public List<String> getRmGroupJaKun() {
		return rmGroupJaKun.size() < 1? null : rmGroupJaKun;
	}

	public List<String> getMeaningEnglish() {
		return meaningEnglish.size() < 1? null : meaningEnglish;
	}

	public List<String> getMeaningFrench() {
		return  meaningEnglish.size() < 1? null : meaningFrench;
	}

	public List<String> getMeaningDutch() {
		return meaningDutch.size() < 1? null : meaningDutch;
	}

	public List<String> getMeaningGerman() {
		return meaningGerman.size() < 1? null : meaningGerman;
	}

	public List<String> getNanori() {
		return nanori.size() < 1? null : nanori;
	}

	public void parseDicRef(String jsonString){
		if(jsonString == null  || jsonString.length() < 1){
			return ;
		}
		Map<String,String> dicRefTemp = new HashMap<String,String>();
    	JSONObject dicRefJson = null;
		try {
			dicRefJson = new JSONObject(jsonString);
		} catch (JSONException e) {
			Log.w("Translation","getting parseJapaneseKeb()  initial expression failed: "+ e.toString());
			return ;
		}
		if(dicRefJson != null){
			Iterator<?> keys = dicRefJson.keys();
			while(keys.hasNext()){
				String key = (String)keys.next();
				String value;
				try {
					value = dicRefJson.getString(key);
					if(key != null && value != null){
						dicRefTemp.put(key, value);
					}
				} catch (JSONException e) {
					Log.w("JapaneseCharacter","parsing dicRef failed");
				}
			}
			if(dicRefTemp != null && dicRefTemp.size() > 0 ){
				for(String key : dicRefTemp.keySet()){
					addDicRef(key, dicRefTemp.get(key));
				}
			}
		}
		
	}
	
	public void parseRmGroupJaOn(String jsonString){
    	if(jsonString == null  || jsonString.length() < 1){
    		return ;
    	}
		List<String> temp = null;
    	JSONArray parseJSON;
		try {
			parseJSON = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("JapaneseCharacter","parsing parseRmGroupJaOn() - initial expression failed: "+ e.toString());
			return ;
		}
		temp = this.parseOneJSONArray(parseJSON);
    	for(String str: temp){
    		this.addRmGroupJaOn(str);
    	}
	}
	
	
	public void parseRmGroupJaKun(String jsonString){
    	if(jsonString == null  || jsonString.length() < 1){
    		return ;
    	}
		List<String> temp = null;
    	JSONArray parseJSON;
		try {
			parseJSON = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("JapaneseCharacter","parsing parseRmGroupJaKun() - initial expression failed: "+ e.toString());
			return ;
		}
		temp = this.parseOneJSONArray(parseJSON);
    	for(String str: temp){
    		this.addRmGroupJaKun(str);
    	}
	}
	
	public void parseMeaningEnglish(String jsonString){
    	if(jsonString == null  || jsonString.length() < 1){
    		return ;
    	}
		List<String> temp = null;
    	JSONArray parseJSON;
		try {
			parseJSON = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("JapaneseCharacter","parsing parseMeaningEnglish() - initial expression failed: "+ e.toString());
			return ;
		}
		temp = this.parseOneJSONArray(parseJSON);
    	for(String str: temp){
    		this.addMeaningEnglish(str);
    	}
	}

	public void parseMeaningFrench(String jsonString){
    	if(jsonString == null  || jsonString.length() < 1){
    		return ;
    	}
		List<String> temp = null;
    	JSONArray parseJSON;
		try {
			parseJSON = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("JapaneseCharacter","parsing parseMeaningFrench() - initial expression failed: "+ e.toString());
			return ;
		}
		temp = this.parseOneJSONArray(parseJSON);
    	for(String str: temp){
    		this.addMeaningFrench(str);
    	}
	}
	
	public void parseMeaningDutch(String jsonString){
    	if(jsonString == null  || jsonString.length() < 1){
    		return ;
    	}
		List<String> temp = null;
    	JSONArray parseJSON;
		try {
			parseJSON = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("JapaneseCharacter","parsing parseMeaningDutch() - initial expression failed: "+ e.toString());
			return ;
		}
		temp = this.parseOneJSONArray(parseJSON);
    	for(String str: temp){
    		this.addMeaningDutch(str);
    	}
	}
	
	public void parseMeaningGerman(String jsonString){
    	if(jsonString == null  || jsonString.length() < 1){
    		return ;
    	}
		List<String> temp = null;
    	JSONArray parseJSON;
		try {
			parseJSON = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("JapaneseCharacter","parsing parseMeaningGerman() - initial expression failed: "+ e.toString());
			return ;
		}
		temp = this.parseOneJSONArray(parseJSON);
    	for(String str: temp){
    		this.addMeaningGerman(str);
    	}
	}
	
	public void parseNanori(String jsonString){
    	if(jsonString == null  || jsonString.length() < 1){
    		return ;
    	}
		List<String> temp = null;
    	JSONArray parseJSON;
		try {
			parseJSON = new JSONArray(jsonString);
		} catch (JSONException e) {
			Log.w("JapaneseCharacter","parsing parseNanori() - initial expression failed: "+ e.toString());
			return ;
		}
		temp = this.parseOneJSONArray(parseJSON);
    	for(String str: temp){
    		this.addNanori(str);
    	}
	}
	
	
	private List<String> parseOneJSONArray(JSONArray sense){
		if(sense == null){
			return null;
		}
		List<String> temp = new ArrayList<String>();
		for(int k = 0; k < sense.length();k++  ){
			String value = null;
			try {
				value = sense.getString(k);
				temp.add(value);
			} catch (JSONException e) {
				Log.w("Translation","getting parseOneSense() expression failed: "+ e.toString());
				e.printStackTrace();
			}
		}
		return temp;
	}
	

	public Bundle createBundleFromJapaneseCharacter(Bundle bundle){
		if(bundle == null){
			bundle = new Bundle();
		}
		if(this.getLiteral() != null && this.getLiteral().length() > 0){
			bundle.putString(SAVE_CHARACTER_LITERAL, this.getLiteral());
		}
		if(this.getRadicalClassic() != 0){
			bundle.putInt(SAVE_CHARACTER_RADICAL, this.getRadicalClassic());
		}
		if(this.getGrade() != 0){	
			bundle.putInt(SAVE_CHARACTER_GRADE, this.getGrade());
		}
		if(this.getStrokeCount() != 0){	
			bundle.putInt(SAVE_CHARACTER_STROKE_COUNT, this.getStrokeCount());
		}
		if(this.getSkip() != null && this.getSkip().length() > 0){
			bundle.putString(SAVE_CHARACTER_SKIP, this.getSkip());
		}
		if(this.getDicRef() != null && this.getDicRef().size() > 0){	
			bundle.putString(SAVE_CHARACTER_DIC_REF,(new JSONObject(this.getDicRef()).toString()));
		}
		if(this.getRmGroupJaOn() != null && this.getRmGroupJaOn().size() > 0){	
			bundle.putString(SAVE_CHARACTER_JA_ON, (new JSONArray(this.getRmGroupJaOn())).toString());
		}
		if(this.getRmGroupJaKun() != null && this.getRmGroupJaKun().size() > 0){	
			bundle.putString(SAVE_CHARACTER_JA_KUN, (new JSONArray(this.getRmGroupJaKun())).toString());
		}
		if(this.getMeaningEnglish() != null && this.getMeaningEnglish().size() > 0){	
			bundle.putString(SAVE_CHARACTER_ENGLISH, (new JSONArray(this.getMeaningEnglish())).toString());
		}	
		if(this.getMeaningFrench() != null && this.getMeaningFrench().size() > 0){	
			bundle.putString(SAVE_CHARACTER_FRENCH, (new JSONArray(this.getMeaningFrench())).toString());
		}	
		if(this.getMeaningDutch() != null && this.getMeaningDutch().size() > 0){	
			bundle.putString(SAVE_CHARACTER_DUTCH, (new JSONArray(this.getMeaningDutch())).toString());
		}	
		if(this.getMeaningGerman() != null && this.getMeaningGerman().size() > 0){	
			bundle.putString(SAVE_CHARACTER_GERMAN, (new JSONArray(this.getMeaningGerman())).toString());
		}	
		if(this.getNanori() != null && this.getNanori().size() > 0){	
			bundle.putString(SAVE_CHARACTER_NANORI, (new JSONArray(this.getNanori())).toString());
		}	
		
		return bundle;
	}
	
	public static JapaneseCharacter newInstanceFromBundle(Bundle bundle){
		JapaneseCharacter japaneseCharacter = new JapaneseCharacter();
		japaneseCharacter.setLiteral(bundle.getString(SAVE_CHARACTER_LITERAL));
		japaneseCharacter.setRadicalClassic(bundle.getInt(SAVE_CHARACTER_RADICAL,0));
		japaneseCharacter.setGrade(bundle.getInt(SAVE_CHARACTER_GRADE,0));
		japaneseCharacter.setStrokeCount(bundle.getInt(SAVE_CHARACTER_STROKE_COUNT,0));
		japaneseCharacter.setSkip(bundle.getString(SAVE_CHARACTER_SKIP));
		japaneseCharacter.parseDicRef(bundle.getString(SAVE_CHARACTER_DIC_REF));
		japaneseCharacter.parseRmGroupJaOn(bundle.getString(SAVE_CHARACTER_JA_ON));
		japaneseCharacter.parseRmGroupJaKun(bundle.getString(SAVE_CHARACTER_JA_KUN));
		japaneseCharacter.parseMeaningEnglish(bundle.getString(SAVE_CHARACTER_ENGLISH));
		japaneseCharacter.parseMeaningFrench(bundle.getString(SAVE_CHARACTER_FRENCH));
		/*
		 *  dutch and german aren't in current kanjidict 2
		 */
		japaneseCharacter.parseMeaningDutch(bundle.getString(SAVE_CHARACTER_DUTCH));
		japaneseCharacter.parseMeaningGerman(bundle.getString(SAVE_CHARACTER_GERMAN));
		japaneseCharacter.parseNanori(bundle.getString(SAVE_CHARACTER_NANORI));
    	
		return japaneseCharacter.getLiteral()!=null && japaneseCharacter.getLiteral().length() > 0 ? japaneseCharacter : null;
	}
	
	
	
}
