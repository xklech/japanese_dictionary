package cz.muni.fi.japanesedictionary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class GlossaryReaderContract extends SQLiteOpenHelper {
	
    private static final int DATABASE_VERSION = 1;
   
    
    // Database Name
    private static final String DATABASE_NAME = "dictionary";
	
    public static final String TABLE_NAME = "translations";
    
	public static abstract class GlossaryEntry implements BaseColumns{
		 public static final String COLUMN_NAME_JAPANESE = "japanese";
		 public static final String COLUMN_NAME_ENGLISH = "english";
		 public static final String COLUMN_NAME_FRENCH = "french";
		 public static final String COLUMN_NAME_DUTCH = "dutch";
		 public static final String COLUMN_NAME_GERMAN = "german";
	}
	
	public GlossaryReaderContract(Context context) {
        super(new DatabaseContext(context), DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
	    "CREATE TABLE " + GlossaryReaderContract.TABLE_NAME + " (" +
	    GlossaryReaderContract.GlossaryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_JAPANESE + TEXT_TYPE + COMMA_SEP +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_ENGLISH + TEXT_TYPE + COMMA_SEP +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_FRENCH + TEXT_TYPE + COMMA_SEP +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_DUTCH + TEXT_TYPE + COMMA_SEP +
	    GlossaryReaderContract.GlossaryEntry.COLUMN_NAME_GERMAN + TEXT_TYPE +
	    " )";// Any other options for the CREATE command

	private static final String SQL_DELETE_ENTRIES =
	    "DROP TABLE IF EXISTS " + GlossaryReaderContract.TABLE_NAME;

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
	public void addTranslation(Translation translation){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		/*values.put(GlossaryEntry.COLUMN_NAME_JAPANESE, translation.getJapanese());
		values.put(GlossaryEntry.COLUMN_NAME_DUTCH, translation.getDutch());
		values.put(GlossaryEntry.COLUMN_NAME_ENGLISH, translation.getEnglish());
		values.put(GlossaryEntry.COLUMN_NAME_FRENCH, translation.getFrench());
		values.put(GlossaryEntry.COLUMN_NAME_GERMAN, translation.getGerman());*/
		db.insert(TABLE_NAME, null, values);
		db.close();
	}
	
	//select translation
	public Translation getTranslation(String japanese){
	    SQLiteDatabase db = this.getReadableDatabase();
	    
	    Cursor cursor = db.query(TABLE_NAME, new String[] { 
	    		GlossaryEntry.COLUMN_NAME_JAPANESE,
	    		GlossaryEntry.COLUMN_NAME_DUTCH, 
	    		GlossaryEntry.COLUMN_NAME_ENGLISH,
	    		GlossaryEntry.COLUMN_NAME_FRENCH,
	    		GlossaryEntry.COLUMN_NAME_GERMAN, }, GlossaryEntry.COLUMN_NAME_JAPANESE + "=?",
	            new String[] { japanese }, null, null, null, null);
	    if (cursor == null){
	    	return null;
	    }
	    if(cursor.getCount()<1){
	    	return null;
	    }
	    cursor.moveToFirst();
	 
	    Translation translation = new Translation();
    	translation.setJapanese(cursor.getString(0));
    	translation.setDutch(cursor.getString(1));
    	translation.setEnglish(cursor.getString(2));
    	translation.setFrench(cursor.getString(3));
    	translation.setGerman(cursor.getString(4));
    	cursor.close();
	    // return translation
	    return translation;
	}
	
}
