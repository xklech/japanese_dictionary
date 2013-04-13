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

	
	private String mLiteral;
	private int mRadicalClassic;
	private int mGrade;
	private int mStrokeCount;
	private String mSkip;
	private Map<String, String> mDicRef;
	private List<String> mRMGroupJaOn;
	private List<String> mRMmGroupJaKun;
	private List<String> mMeaningEnglish;
	private List<String> mMeaningFrench;
	/*
	 *  dutch and german aren't in current kanjidict 2
	 */
	private List<String> mMeaningDutch;
	private List<String> mMeaningGerman;
	
	private List<String> mNanori;
	
	@Override
	public String toString() {
		return "JapaneseCharacter [literal=" + mLiteral + ", radicalClassic="
				+ mRadicalClassic + ", grade=" + mGrade + ", strokeCount="
				+ mStrokeCount + ", dicRef=" + mDicRef + ", rmGroupJaOn="
				+ mRMGroupJaOn + ", rmGroupJaKun=" + mRMmGroupJaKun
				+ ", meaningEnglish=" + mMeaningEnglish + ", meaningFrench="
				+ mMeaningFrench + ", meaningDutch=" + mMeaningDutch
				+ ", meaningGerman=" + mMeaningGerman + ", nanori=" + mNanori
				+ "]";
	}
	
	public JapaneseCharacter(){
		mLiteral = null;
		mRadicalClassic = 0;
		mGrade = 0;
		mStrokeCount = 0;
		mDicRef = new HashMap<String,String>();
		mRMGroupJaOn = new ArrayList<String>();
		mRMmGroupJaKun = new ArrayList<String>();
		mMeaningEnglish = new ArrayList<String>();
		mMeaningFrench = new ArrayList<String>();
		mMeaningDutch = new ArrayList<String>();
		mMeaningGerman = new ArrayList<String>();
		mNanori = new ArrayList<String>();
	}

	public void setLiteral(String value){
		this.mLiteral = value;
	}
	
	public void setRadicalClassic(int value){
		this.mRadicalClassic = value;
	}
	
	public void setGrade(int value){
		this.mGrade = value;
	}
	
	public void setStrokeCount(int value){
		this.mStrokeCount = value;
	}

	public void setSkip(String skip) {
		this.mSkip = skip;
	}

	public void addDicRef(String key,String value){
		if(key == null || key.length() < 1 || value == null || value.length() < 1){
			return ;
		}
		mDicRef.put(key, value);
	}
	
	public void addRmGroupJaOn(String value){
		if(value == null || value.length() < 1){
			return;
		}
		mRMGroupJaOn.add(value);
	}
	
	public void addRmGroupJaKun(String value){
		if(value == null || value.length() < 1){
			return;
		}
		mRMmGroupJaKun.add(value);
	}
	
	public void addMeaningEnglish(String value){
		if(value == null || value.length() < 1){
			return;
		}
		mMeaningEnglish.add(value);
	}
	
	public void addMeaningFrench(String value){
		if(value == null || value.length() < 1){
			return;
		}
		mMeaningFrench.add(value);
	}	
	
	public void addMeaningDutch(String value){
		if(value == null || value.length() < 1){
			return;
		}
		mMeaningDutch.add(value);
	}	

	public void addMeaningGerman(String value){
		if(value == null || value.length() < 1){
			return;
		}
		mMeaningGerman.add(value);
	}	
	
	public void addNanori(String value){
		if(value == null || value.length() < 1){
			return;
		}
		mNanori.add(value);
	}
	
	public String getLiteral() {
		return mLiteral;
	}

	public int getRadicalClassic() {
		return mRadicalClassic;
	}

	public int getGrade() {
		return mGrade;
	}

	public int getStrokeCount() {
		return mStrokeCount;
	}

	public String getSkip() {
		return mSkip;
	}
	
	public Map<String, String> getDicRef() {
		return mDicRef.size() < 1? null : mDicRef;
	}

	public List<String> getRmGroupJaOn() {
		return mRMGroupJaOn.size() < 1? null : mRMGroupJaOn;
	}

	public List<String> getRmGroupJaKun() {
		return mRMmGroupJaKun.size() < 1? null : mRMmGroupJaKun;
	}

	public List<String> getMeaningEnglish() {
		return mMeaningEnglish.size() < 1? null : mMeaningEnglish;
	}

	public List<String> getMeaningFrench() {
		return  mMeaningFrench.size() < 1? null : mMeaningFrench;
	}

	public List<String> getMeaningDutch() {
		return mMeaningDutch.size() < 1? null : mMeaningDutch;
	}

	public List<String> getMeaningGerman() {
		return mMeaningGerman.size() < 1? null : mMeaningGerman;
	}

	public List<String> getNanori() {
		return mNanori.size() < 1? null : mNanori;
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
