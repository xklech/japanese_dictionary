package cz.muni.fi.japanesedictionary.database;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import cz.muni.japanesedictionary.entity.Translation;

public class GlossaryReaderContract extends SQLiteOpenHelper {
	
    private static final int DATABASE_VERSION = 1;
   
    
    // Database Name
    private static final String DATABASE_NAME = "JapaneseDictionary.db";
	

    
	public static abstract class GlossaryEntry implements BaseColumns{
	    public static final String TABLE_NAME = "last_translations";
		 public static final String COLUMN_NAME_JAPANESE_KEB = "japanese_keb";
		 public static final String COLUMN_NAME_JAPANESE_REB = "japanese_reb";
		 public static final String COLUMN_NAME_ENGLISH = "english";
		 public static final String COLUMN_NAME_FRENCH = "french";
		 public static final String COLUMN_NAME_DUTCH = "dutch";
		 public static final String COLUMN_NAME_GERMAN = "german";
	}
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
	    "CREATE TABLE " + GlossaryReaderContract.GlossaryEntry.TABLE_NAME + " (" +
	    GlossaryReaderContract.GlossaryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_JAPANESE_KEB + TEXT_TYPE + COMMA_SEP +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_JAPANESE_REB + TEXT_TYPE + COMMA_SEP +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_ENGLISH + TEXT_TYPE + COMMA_SEP +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_FRENCH + TEXT_TYPE + COMMA_SEP +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_DUTCH + TEXT_TYPE + COMMA_SEP +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_GERMAN + TEXT_TYPE +
	    " )";// Any other options for the CREATE command

	private static final String SQL_DELETE_ENTRIES =
	    "DROP TABLE IF EXISTS " + GlossaryReaderContract.GlossaryEntry.TABLE_NAME;

	
	
	
	public GlossaryReaderContract(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		 db.execSQL(GlossaryReaderContract.SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(GlossaryReaderContract.SQL_DELETE_ENTRIES);
		onCreate(db);
	}
	
	// add translation
	public void saveTranslation(Translation translation){
		SQLiteDatabase db = this.getWritableDatabase();	
		
		ContentValues values = new ContentValues();
		if(translation.getJapaneseKeb() != null && translation.getJapaneseKeb().size() > 0){
			values.put(GlossaryEntry.COLUMN_NAME_JAPANESE_KEB, (new JSONArray(translation.getJapaneseKeb())).toString());
		}
		if(translation.getJapaneseReb() != null && translation.getJapaneseReb().size() > 0){
			values.put(GlossaryEntry.COLUMN_NAME_JAPANESE_REB, (new JSONArray(translation.getJapaneseReb())).toString());
		}
		if(translation.getDutchSense() != null && translation.getDutchSense().size() > 0){	
			JSONArray sense = convertToJSON(translation.getDutchSense());
			values.put(GlossaryEntry.COLUMN_NAME_DUTCH,sense.toString());
		}
		
		if(translation.getEnglishSense() != null && translation.getEnglishSense().size() > 0){	
			JSONArray sense = convertToJSON(translation.getEnglishSense());
			values.put(GlossaryEntry.COLUMN_NAME_ENGLISH, sense.toString());
		}
		if(translation.getFrenchSense() != null && translation.getFrenchSense().size() > 0){	
			JSONArray sense = convertToJSON(translation.getFrenchSense());
			values.put(GlossaryEntry.COLUMN_NAME_FRENCH, sense.toString());
		}
		if(translation.getGermanSense() != null && translation.getGermanSense().size() > 0){
			JSONArray sense = convertToJSON(translation.getGermanSense());
			values.put(GlossaryEntry.COLUMN_NAME_GERMAN, sense.toString());
		}
		Log.i("GlossaryReaderContract","Save translation values: "+values.toString());
		long returnedId = db.insert(GlossaryEntry.TABLE_NAME,null, values);
		if(returnedId == -1){
			Log.e("GlossaryReaderContract", "Error inserting Transaltion: "+translation.toString()+" Values: "+values.toString());
		}
		db.close();
	}
	
	//select translation
	public List<Translation> getLastTranslations(int count){
		if(count < 1){
			return null;
		}
		List<Translation> translationsReturn = new ArrayList<Translation>();
		
	    SQLiteDatabase db = this.getReadableDatabase();
	    
	    String[] projection = new String[]{	    		
	    		GlossaryEntry.COLUMN_NAME_JAPANESE_KEB,
	    		GlossaryEntry.COLUMN_NAME_JAPANESE_REB,
	    		GlossaryEntry.COLUMN_NAME_DUTCH, 
	    		GlossaryEntry.COLUMN_NAME_ENGLISH,
	    		GlossaryEntry.COLUMN_NAME_FRENCH,
	    		GlossaryEntry.COLUMN_NAME_GERMAN,
	    		};
	    
	    Cursor cursor = db.query(
	    		true, //distinct - not same translations
	    		GlossaryEntry.TABLE_NAME, // table name
	    		projection,  // projection - rows
	    		null,	//selection
	            null, 	//selection args
	            null, 	//group
	            null, 	//having
	            "_id DESC", 	//order by - ordered by id descendant
	            String.valueOf(count));	//limit - 10 last records 
	    if (cursor == null){
	    	return null;
	    }
	    if(cursor.getCount()<1){
	    	return null;
	    }
	    cursor.moveToFirst();
	    do{
	    	Translation translation = new Translation();
	    	String japaneseKeb = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntry.COLUMN_NAME_JAPANESE_KEB));
	    	String japaneseReb = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntry.COLUMN_NAME_JAPANESE_REB));
	    	String english = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntry.COLUMN_NAME_ENGLISH));
	    	String french = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntry.COLUMN_NAME_FRENCH));
	    	String dutch = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntry.COLUMN_NAME_DUTCH));
	    	String german = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntry.COLUMN_NAME_GERMAN));   
	    	
	    	translation.parseJapaneseKeb(japaneseKeb);
	    	translation.parseJapaneseReb(japaneseReb);
	    	translation.parseEnglish(english);
	    	translation.parseFrench(french);
	    	translation.parseDutch(dutch);
	    	translation.parseGerman(german);

	    	translationsReturn.add(translation);
	    }while(cursor.moveToNext());

    	cursor.close();
	    return (translationsReturn == null || translationsReturn.size()<1)? null : translationsReturn;
	}
	
	/**
	 * Taks list of senses and convert it to ona JSONArray
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

	
}
