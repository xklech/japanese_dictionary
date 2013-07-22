package cz.muni.fi.japanesedictionary.interfaces;

import com.actionbarsherlock.view.MenuItem;

/**
 * Created by Jarek on 21.7.13.
 */
public interface OnCreateFavoriteListener extends OnCreateTranslationListener{

    public MenuItem getFavoriteMenuItem();

}
