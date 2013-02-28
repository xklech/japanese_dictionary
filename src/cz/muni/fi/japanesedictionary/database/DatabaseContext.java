package cz.muni.fi.japanesedictionary.database;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseContext extends ContextWrapper{

	public DatabaseContext(Context base) {
		super(base);
	}

	@Override
	public File getDatabasePath(String name) 
	{
	    File sdcard = getApplicationContext().getExternalCacheDir();    
	    String dbfile = sdcard.getAbsolutePath() + File.separator+ name;
	    if (!dbfile.endsWith(".db"))
	    {
	        dbfile += ".db" ;
	    }

	    File result = new File(dbfile);

	    if (!result.getParentFile().exists())
	    {
	        result.getParentFile().mkdirs();
	    }


	    return result;
	}
	
	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) 
	{
	    SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
	    // SQLiteDatabase result = super.openOrCreateDatabase(name, mode, factory);
	    return result;
	}
	
}

