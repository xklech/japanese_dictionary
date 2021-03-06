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

package cz.muni.fi.japanesedictionary.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import cz.muni.fi.japanesedictionary.entity.Translation;

/**
 * SQLite helper for managing database.
 * @author Jaroslav Klech
 *
 */
public class GlossaryReaderContract extends SQLiteOpenHelper {
	
    private static final int DATABASE_VERSION = 13;

    // Database Name
    private static final String DATABASE_NAME = "JapaneseDictionary.db";
	
    private static final String LOG_TAG = "GlossaryReaderContract";

    public static abstract class GlossaryEntryFavorite implements BaseColumns{
        public static final String TABLE_NAME = "favorite";
        public static final String COLUMN_NAME_JAPANESE_KEB = "japanese_keb";
        public static final String COLUMN_NAME_JAPANESE_REB = "japanese_reb";
        public static final String COLUMN_NAME_ENGLISH = "english";
        public static final String COLUMN_NAME_FRENCH = "french";
        public static final String COLUMN_NAME_DUTCH = "dutch";
        public static final String COLUMN_NAME_GERMAN = "german";
        public static final String COLUMN_NAME_RUSSIAN = "russian";
        public static final String COLUMN_NAME_NOTE = "note";
        public static final String COLUMN_NAME_FAVORITE = "favorite";
        public static final String COLUMN_NAME_RUBY = "ruby";
        public static final String COLUMN_NAME_LAST_VIEWED = "last_viewed";

        public static final String COLUMN_NAME_PRIORITIZED = "prioritized";
    }

	
	private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES_FAVORITE=
            "CREATE TABLE " + GlossaryReaderContract.GlossaryEntryFavorite.TABLE_NAME + " (" +
            GlossaryReaderContract.GlossaryEntryFavorite._ID + " TEXT PRIMARY KEY," +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_KEB + TEXT_TYPE + COMMA_SEP +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_REB + TEXT_TYPE + COMMA_SEP +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_ENGLISH + TEXT_TYPE + COMMA_SEP +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_FRENCH + TEXT_TYPE + COMMA_SEP +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_DUTCH + TEXT_TYPE + COMMA_SEP +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_GERMAN + TEXT_TYPE + COMMA_SEP +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_RUSSIAN + TEXT_TYPE + COMMA_SEP +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_NOTE + TEXT_TYPE + COMMA_SEP +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_FAVORITE + INTEGER_TYPE + COMMA_SEP+
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_RUBY + TEXT_TYPE + COMMA_SEP+
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_PRIORITIZED + INTEGER_TYPE + COMMA_SEP +
            GlossaryReaderContract.GlossaryEntryFavorite.COLUMN_NAME_LAST_VIEWED + INTEGER_TYPE +
            " );";

    private static final String SQL_DELETE_ENTRIES_FAVORITE =
        "DROP TABLE IF EXISTS " + GlossaryReaderContract.GlossaryEntryFavorite.TABLE_NAME;

	
	
	
	public GlossaryReaderContract(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(GlossaryReaderContract.SQL_CREATE_ENTRIES_FAVORITE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(GlossaryReaderContract.SQL_DELETE_ENTRIES_FAVORITE);
		onCreate(db);
	}
	
	/**
	 * Saves given translation to database
	 * @param translation translation to be saved into database
	 */
	public void saveTranslation(Translation translation){
		SQLiteDatabase db = this.getWritableDatabase();
        if(db == null){
            Log.w(LOG_TAG, "Error inserting Transaltion, database is null");
            return ;
        }
        if(!db.isOpen()){
            Log.w(LOG_TAG, "Error inserting Transaltion, database is closed");
            return ;
        }

        ContentValues values = translation.createContentValuesFromTranslation();
        int updateCount = db.update(GlossaryEntryFavorite.TABLE_NAME, values, GlossaryEntryFavorite._ID + " = ?", new String[]{translation.getIndexHash()});
        if(updateCount == 0){
            //insert new
            Log.i(LOG_TAG, "Is Not factorised, doesn't exist, insert new values");
            values.put(GlossaryEntryFavorite._ID, translation.getIndexHash());
            long returnedId = db.insert(GlossaryEntryFavorite.TABLE_NAME, null, values);
            if(returnedId == -1){
                Log.e(LOG_TAG, "Error inserting Translation: " + translation.toString() + " Values: " + values.toString());
            }
        }

		
	}
	
	/**
	 * Returns last translations saved in database
	 * @param count count of translation to be returned
	 * @return List<Translation> list of translation or null if there aren't any translations
	 */
	public List<Translation> getLastTranslations(int count){
		if(count < 1){
			return null;
		}
		
		
	    SQLiteDatabase db = this.getReadableDatabase();
        if(db == null){
            Log.w(LOG_TAG, "Error retreiving Transaltions, database is null");
            return null;
        }
		if(!db.isOpen()){
			Log.w(LOG_TAG, "Error retreiving Transaltions, database is closed");
			return null;
		}

	    String[] projection = new String[]{
                GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_KEB,
                GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_REB,
                GlossaryEntryFavorite.COLUMN_NAME_DUTCH,
                GlossaryEntryFavorite.COLUMN_NAME_ENGLISH,
                GlossaryEntryFavorite.COLUMN_NAME_FRENCH,
                GlossaryEntryFavorite.COLUMN_NAME_GERMAN,
                GlossaryEntryFavorite.COLUMN_NAME_RUSSIAN,
                GlossaryEntryFavorite.COLUMN_NAME_PRIORITIZED,
                GlossaryEntryFavorite.COLUMN_NAME_RUBY
	    		};
	    
	    Cursor cursor = db.query(
	    		true, //distinct - not same translations
                GlossaryEntryFavorite.TABLE_NAME, // table name
	    		projection,  // projection - rows
	    		null,	//selection
	            null, 	//selection args
	            null, 	//group
	            null, 	//having
                GlossaryEntryFavorite.COLUMN_NAME_LAST_VIEWED + " DESC", 	//order by - ordered by id descendant
	            String.valueOf(count));	//limit - 10 last records 
        return createFromCursor(cursor);
	}

    public boolean isFavorite(Translation translation){
        if(translation == null){
            return false;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        if(db == null){
            Log.w(LOG_TAG, "Error is Favorite, database is null");
            return false;
        }
        if(!db.isOpen()){
            Log.w(LOG_TAG, "Error is Favorite, database is closed");
            return false;
        }

        Cursor cursor = db.query(
                true, //distinct - not same translations
                GlossaryEntryFavorite.TABLE_NAME, // table name
                new String[]{GlossaryEntryFavorite.COLUMN_NAME_FAVORITE},  // projection - rows
                "_ID = ?",	//selection
                new String[]{translation.getIndexHash()}, 	//selection args
                null, 	//group
                null, 	//having
                null, 	//order by - ordered by id descendant
                null);	//limit - 10 last records

        if (cursor == null){
            return false;
        }
        if(cursor.getCount() < 1){
            cursor.close();
            return false;
        }

        cursor.moveToFirst();
        int favorite = cursor.getInt(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_FAVORITE));
        cursor.close();
        return favorite > 0;

    }
	
    public boolean changeFavorite(Translation translation){
        if(translation == null){
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        if(db == null){
            Log.w(LOG_TAG, "Error changing Translations, database is null");
            return false;
        }
        if(!db.isOpen()){
            Log.w(LOG_TAG, "Error changing Translations, database is closed");
            return false;
        }


        if(isFavorite(translation)){
            Log.i(LOG_TAG, "Is favorized, update to 0");
            ContentValues values = new ContentValues();
            values.put(GlossaryEntryFavorite.COLUMN_NAME_FAVORITE, 0);
            db.update(GlossaryEntryFavorite.TABLE_NAME, values ,GlossaryEntryFavorite._ID + " = ?",new String[]{translation.getIndexHash()});
            return false;
        }else{
            Log.i(LOG_TAG, "Is Not favorized, update to 1");
            ContentValues values = new ContentValues();
            values.put(GlossaryEntryFavorite.COLUMN_NAME_FAVORITE, 1);
            int updateCount = db.update(GlossaryEntryFavorite.TABLE_NAME,values, "_ID = ?",new String[]{translation.getIndexHash()});
            if(updateCount == 0){
                //insert new
                Log.i(LOG_TAG, "Is Not favorized, doesn¨t exist, insert new values");
                ContentValues valuesInsert = translation.createContentValuesFromTranslation();
                valuesInsert.put(GlossaryEntryFavorite.COLUMN_NAME_FAVORITE,1);
                valuesInsert.put(GlossaryEntryFavorite._ID,translation.getIndexHash());
                db.insert(GlossaryEntryFavorite.TABLE_NAME, null, valuesInsert);
            }
            return true;
        }

    }

    public List<Translation> selectFavoriteTranslations(){
        SQLiteDatabase db = this.getReadableDatabase();
        if(db == null){
            Log.w(LOG_TAG, "Error selecting favorised translations, database is null");
            return null;
        }
        if(!db.isOpen()){
            Log.w(LOG_TAG, "Error selecting favorised translations, database is closed");
            return null;
        }
        String[] projection = new String[]{
                GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_KEB,
                GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_REB,
                GlossaryEntryFavorite.COLUMN_NAME_DUTCH,
                GlossaryEntryFavorite.COLUMN_NAME_ENGLISH,
                GlossaryEntryFavorite.COLUMN_NAME_FRENCH,
                GlossaryEntryFavorite.COLUMN_NAME_GERMAN,
                GlossaryEntryFavorite.COLUMN_NAME_RUSSIAN,
                GlossaryEntryFavorite.COLUMN_NAME_PRIORITIZED,
                GlossaryEntryFavorite.COLUMN_NAME_RUBY
        };
        Cursor cursor = db.query(
                true, //distinct - not same translations
                GlossaryEntryFavorite.TABLE_NAME, // table name
                projection,  // projection - rows
                GlossaryEntryFavorite.COLUMN_NAME_FAVORITE + " = 1",	//selection
                null, 	//selection args
                null, 	//group
                null, 	//having
                GlossaryEntryFavorite.COLUMN_NAME_LAST_VIEWED + " DESC", 	//order by - ordered by id desc endant
                null);	//limit - 10 last records

        return createFromCursor(cursor);
    }

    public String getNote(Translation translation){
        if(translation == null){
            return null;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        if(db == null){
            Log.e(LOG_TAG, "Error getNote, database is null");
            return null;
        }
        if(!db.isOpen()){
            Log.e(LOG_TAG, "Error getNote, database is closed");
            return null;
        }

        Cursor cursor = db.query(
                true, //distinct - not same translations
                GlossaryEntryFavorite.TABLE_NAME, // table name
                new String[]{GlossaryEntryFavorite.COLUMN_NAME_NOTE},  // projection - rows
                "_ID = ?",	//selection
                new String[]{translation.getIndexHash()}, 	//selection args
                null, 	//group
                null, 	//having
                null, 	//order by - ordered by id descendant
                null);	//limit - 10 last records

        if (cursor == null){
            return null;
        }
        if(cursor.getCount()<1){
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        String note = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_NOTE));
        cursor.close();
        return note;
    }

    public String saveNote(Translation translation, String note){
        if(translation == null){
            return null;
        }
        if(note == null){
            return null;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        if(db == null){
            Log.e(LOG_TAG, "Error getNote, database is null");
            return null;
        }
        if(!db.isOpen()){
            Log.e(LOG_TAG, "Error getNote, database is closed");
            return null;
        }
        Log.i(LOG_TAG, "save note: "+note);
        ContentValues values = new ContentValues();
        values.put(GlossaryEntryFavorite.COLUMN_NAME_NOTE,note);
        int count = db.update(GlossaryEntryFavorite.TABLE_NAME,values,"_ID = ?",new String[]{translation.getIndexHash()});
        if(count == 0){
            return getNote(translation);
        }
        return note;

    }

    private List<Translation> createFromCursor(Cursor cursor){
        if (cursor == null){
            return null;
        }
        if(cursor.getCount()<1){
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        List<Translation> translationsReturn = new ArrayList<Translation>();
        do{
            Translation translation = new Translation();
            String japaneseKeb = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_KEB));
            String japaneseReb = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_JAPANESE_REB));
            String english = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_ENGLISH));
            String french = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_FRENCH));
            String dutch = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_DUTCH));
            String german = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_GERMAN));
            String russian = cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_RUSSIAN));

            translation.parseJapaneseKeb(japaneseKeb);
            translation.parseJapaneseReb(japaneseReb);
            translation.parseEnglish(english);
            translation.parseFrench(french);
            translation.parseDutch(dutch);
            translation.parseGerman(german);
            translation.parseRussian(russian);
            translation.setPrioritized(cursor.getInt(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_PRIORITIZED)) > 0);
            translation.setRuby(cursor.getString(cursor.getColumnIndexOrThrow(GlossaryEntryFavorite.COLUMN_NAME_RUBY)));

            translationsReturn.add(translation);
        }while(cursor.moveToNext());

        cursor.close();

        return (translationsReturn.size()<1)? null : translationsReturn;

    }


	
}
