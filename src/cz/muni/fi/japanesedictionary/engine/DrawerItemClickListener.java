package cz.muni.fi.japanesedictionary.engine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.main.FavoriteActivity;
import cz.muni.fi.japanesedictionary.main.MainActivity;

/**
 * Created by Jarek on 4.8.13.
*/


public class DrawerItemClickListener implements ListView.OnItemClickListener {
    private static final String LOG_TAG = "DrawerItemClickListener";

    private Context mContext;

    public DrawerItemClickListener(Context context){
        mContext = context;
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        Log.i(LOG_TAG, "Click position: "+position);

        switch(position){
            case 0:
                Intent intentMain = new Intent(mContext,MainActivity.class);
                intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intentMain.putExtra("search",true);
                mContext.startActivity(intentMain);
                break;
            case 1:
                Log.i(LOG_TAG, "Launching Favorite activity");
                Intent intentFavorites = new Intent(mContext,FavoriteActivity.class);
                intentFavorites.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intentFavorites);
                break;
            case 2:
                Log.i(LOG_TAG, "Launching Main activity");
                Intent intentMainLast = new Intent(mContext,MainActivity.class);
                intentMainLast.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intentMainLast.putExtra("search",false);
                mContext.startActivity(intentMainLast);
                break;

        }
    }

}


