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

package cz.muni.fi.japanesedictionary.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.List;
import java.util.Map;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.DBAsyncTask;
import cz.muni.fi.japanesedictionary.engine.CharacterLoader;
import cz.muni.fi.japanesedictionary.engine.FavoriteChanger;
import cz.muni.fi.japanesedictionary.engine.FavoriteLoader;
import cz.muni.fi.japanesedictionary.engine.NoteLoader;
import cz.muni.fi.japanesedictionary.engine.NoteSaver;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.interfaces.OnCreateTranslationListener;
import cz.muni.fi.japanesedictionary.parser.RomanizationEnum;

/**
 * Fragment which displays translation info
 * @author Jaroslav Klech
 *
 */

public class DisplayTranslation extends SherlockFragment {
	
    private static final String LOG_TAG = "DisplayTranslation";
	
	private OnCreateTranslationListener mCallbackTranslation;
	private Translation mTranslation = null;
	private Map<String, JapaneseCharacter> mCharacters = null;
	private LayoutInflater mInflater = null;
	

    private boolean mActualMenuItems;
	private boolean mEnglish;
    private boolean mFrench;        
    private boolean mDutch;
    private boolean mGerman;

    private MenuItem mFavorite;

    private MenuItem mNote;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	mCallbackTranslation = (OnCreateTranslationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCreateTranslationListener");
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.display_translation, null);
	}

	@Override
	public void onStart() {
        if(!mActualMenuItems && mTranslation != null){
            updateMenu();
        }
		updateTranslation();
		super.onStart();
	}

    @Override
    public void onPause() {
        mActualMenuItems = false;
        super.onPause();
        
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState != null){
			mTranslation = Translation.newInstanceFromBundle(savedInstanceState);
			Log.i(LOG_TAG,"Loading from saved state, restoring translation");
		}

		setHasOptionsMenu(true);
		mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        super.onCreate(savedInstanceState);
	}


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite:
                Log.i(LOG_TAG, "favorite changed");
                mFavorite.setEnabled(false);
                FavoriteChanger changeFavorite = new FavoriteChanger(mCallbackTranslation.getDatabse(), mFavorite, this);
                changeFavorite.execute(mTranslation);
                return true;
            case R.id.ab_note:
                Log.i(LOG_TAG, "notes opened");
                TextView note = (TextView) getActivity().findViewById(R.id.translation_note_text);
                if(note != null){
                    showNoteAlertBox(note.getText().toString());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
	public void onSaveInstanceState(Bundle outState) {
		
		if(mTranslation!= null){
			outState = mTranslation.createBundleFromTranslation(outState);
		}
		Log.i(LOG_TAG,"Saving instance ");
		super.onSaveInstanceState(outState);
	}	


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		if(savedInstanceState == null){
			Bundle bundle = getArguments();
			if(bundle != null){
				mTranslation =  Translation.newInstanceFromBundle(bundle);
			}
		}

		super.onViewCreated(view, savedInstanceState);
	}
	
	/**
	 * Change displayed transwlation
	 * 
	 * @param tran translation to be changed
	 */
	public void setTranslation(Translation tran){
		this.mTranslation = tran;
		if(this.isVisible() && mTranslation != null){
            if(mFavorite != null && mNote != null){
                mFavorite.setEnabled(false);
                mNote.setEnabled(false);
                updateMenu();
            }
			updateTranslation();
		}
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        mFavorite = menu.findItem(R.id.favorite);
        mNote = menu.findItem(R.id.ab_note);
        if(mTranslation == null){
            Bundle bundle = getArguments();
            if(bundle != null){
                mTranslation =  Translation.newInstanceFromBundle(bundle);
            }
        }
        if(mTranslation != null && !mActualMenuItems){
            updateMenu();
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
	 * Updates Fragment view acording to saved translation.
	 * 
	 */
	public void updateTranslation(){
		Log.i(LOG_TAG,"Update translation");
		
		LinearLayout layoutSelect = (LinearLayout) getView().findViewById(R.id.translation_select);
		View layoutTranslation = getView().findViewById(R.id.translation_container);
		if(mTranslation == null){
			layoutSelect.setVisibility(View.VISIBLE);
			layoutTranslation.setVisibility(View.GONE);
			return;
		}
		layoutSelect.setVisibility(View.GONE);
		layoutTranslation.setVisibility(View.VISIBLE);
		
		updateLanguages();
		
		TextView read = (TextView)getView().findViewById(R.id.translation_read);
		TextView write = (TextView)getView().findViewById(R.id.translation_write);
		
		
		
		
		TextView alternative = (TextView)getView().findViewById(R.id.translation_alternative);
		StringBuilder alternativeStrBuilder = new StringBuilder();
        if(mTranslation.getJapaneseReb() != null){
        	String reading = mTranslation.getJapaneseReb().get(0);
        	read.setText(reading);
        	getSherlockActivity().getSupportActionBar().setTitle(reading);
        	DBAsyncTask saveTranslation   = new DBAsyncTask(mCallbackTranslation.getDatabse());
        	if(saveTranslation != null){
        		saveTranslation.execute(mTranslation);
        	}
        	TextView romaji = (TextView)getView().findViewById(R.id.translation_romaji);
        	romaji.setText(RomanizationEnum.Hepburn.toRomaji(reading.replaceAll(".(?!$)", "$0 ")));
        	
        	int sizeReb = mTranslation.getJapaneseReb().size();
        	if(sizeReb > 1){
        		for(int i = 1;i < sizeReb;i++){
        			alternativeStrBuilder.append(mTranslation.getJapaneseReb().get(i));
        			if(i+1 < sizeReb){
        				alternativeStrBuilder.append(", ");
        			}
        		}
        	}
        }
        if(mTranslation.getJapaneseKeb() != null){
        	int size_keb = mTranslation.getJapaneseKeb().size();
        	if(size_keb > 1){
        		if(alternativeStrBuilder.length() > 0){
        			alternativeStrBuilder.append(", ");
        		}
        		for(int i = 1;i < size_keb;i++){
        			alternativeStrBuilder.append(mTranslation.getJapaneseKeb().get(i));
        			if(i+1 < size_keb){
        				alternativeStrBuilder.append(", ");
        			}
        		}
        	}
        }
        if(alternativeStrBuilder.length() > 0){
        	alternative.setText(alternativeStrBuilder);  
	       	getView().findViewById(R.id.translation_alternative_container).setVisibility(View.VISIBLE);
		}else{
			getView().findViewById(R.id.translation_alternative_container).setVisibility(View.GONE);
		}
        
        
        String writeCharacters = null;
        if(mTranslation.getJapaneseKeb() != null && mTranslation.getJapaneseKeb().size() > 0){
        	writeCharacters = mTranslation.getJapaneseKeb().get(0);
        	write.setText(writeCharacters);
        	write.setVisibility(View.VISIBLE);
        }else{
        	write.setVisibility(View.GONE);
        }
        

    	if(mInflater != null){
    		LinearLayout translationsContainer = (LinearLayout)getView().findViewById(R.id.translation_translation_container);
    		translationsContainer.removeAllViews();
    		if((mEnglish || (!mEnglish && !mFrench && !mDutch && ! mGerman)) && mTranslation.getEnglishSense()!= null && mTranslation.getEnglishSense().size() > 0){
		        	View translation_language = mInflater.inflate(R.layout.translation_language, null);
	    			TextView textView = (TextView) translation_language.findViewById(R.id.translation_language);
	    			textView.setText(getString(R.string.language_english));
	    			translationsContainer.addView(translation_language);
		        	for(List<String> tran: mTranslation.getEnglishSense()){
		        		int tran_size = tran.size();
		        		if(tran_size>0){
		        			int i =0;
		        			StringBuilder strBuilder = new StringBuilder();
		        			for(String str:tran){
		        				strBuilder.append(str);
		        				i++;
		        				if(i < tran_size){
		        					strBuilder.append(", ");
		        				}
		        			}
		        			View translation_ll = mInflater.inflate(R.layout.translation_line, null);
		        			TextView tView = (TextView) translation_ll.findViewById(R.id.translation_translation);
		        			tView.setText(strBuilder.toString());
		        			translationsContainer.addView(translation_ll);
		        		}
		        		
		        	}
	        }
	        if(mFrench && mTranslation.getFrenchSense()!= null && mTranslation.getFrenchSense().size() > 0){
	        	View translation_language = mInflater.inflate(R.layout.translation_language, null);
    			TextView textView = (TextView) translation_language.findViewById(R.id.translation_language);
    			textView.setText(getString(R.string.language_french));
    			translationsContainer.addView(translation_language);
		        	for(List<String> tran: mTranslation.getFrenchSense()){
		        		int tran_size = tran.size();
		        		if(tran_size>0){
		        			int i =0;
		        			StringBuilder strBuilder = new StringBuilder();
		        			for(String str:tran){
		        				strBuilder.append(str);
		        				i++;
		        				if(i < tran_size){
		        					strBuilder.append(", ");
		        				}
		        			}
		        			View translation_ll = mInflater.inflate(R.layout.translation_line, null);
		        			TextView tView = (TextView) translation_ll.findViewById(R.id.translation_translation);
		        			tView.setText(strBuilder.toString());
		        			translationsContainer.addView(translation_ll);
		        		}
		        	}
	        }
	        if(mDutch && mTranslation.getDutchSense()!= null && mTranslation.getDutchSense().size() > 0){
	        	View translation_language = mInflater.inflate(R.layout.translation_language, null);
    			TextView textView = (TextView) translation_language.findViewById(R.id.translation_language);
    			textView.setText(getString(R.string.language_dutch));
    			translationsContainer.addView(translation_language);
		        	for(List<String> tran: mTranslation.getDutchSense()){
		        		int tran_size = tran.size();
		        		if(tran_size>0){
		        			int i =0;
		        			StringBuilder strBuilder = new StringBuilder();
		        			for(String str:tran){
		        				strBuilder.append(str);
		        				i++;
		        				if(i < tran_size){
		        					strBuilder.append(", ");
		        				}
		        			}
		        			View translation_ll = mInflater.inflate(R.layout.translation_line, null);
		        			TextView tView = (TextView) translation_ll.findViewById(R.id.translation_translation);
		        			tView.setText(strBuilder.toString());
		        			translationsContainer.addView(translation_ll);
		        		}
		        	}
	        }
	        if(mGerman && mTranslation.getGermanSense() != null && mTranslation.getGermanSense().size() > 0){
	        	View translation_language = mInflater.inflate(R.layout.translation_language, null);
    			TextView textView = (TextView) translation_language.findViewById(R.id.translation_language);
    			textView.setText(getString(R.string.language_german));
    			translationsContainer.addView(translation_language);
		        	for(List<String> tran: mTranslation.getGermanSense()){
		        		int tran_size = tran.size();
		        		if(tran_size>0){
		        			int i =0;
		        			StringBuilder strBuilder = new StringBuilder();
		        			for(String str:tran){
		        				strBuilder.append(str);
		        				i++;
		        				if(i < tran_size){
		        					strBuilder.append(", ");
		        				}
		        			}
		        			View translation_ll = mInflater.inflate(R.layout.translation_line, null);
		        			TextView tView = (TextView) translation_ll.findViewById(R.id.translation_translation);
		        			tView.setText(strBuilder.toString());
		        			translationsContainer.addView(translation_ll);
		        		}
		        	}
	        }
	        getView().findViewById(R.id.translation_kanji_container).setVisibility(View.GONE);
	        mCharacters = null;
	        if(writeCharacters != null){
	        	// write single characters
	        	if(writeCharacters.length() > 0){
	        		CharacterLoader charLoader  = new CharacterLoader(getActivity().getApplicationContext(),this);
	        		charLoader.execute(writeCharacters);
	        	}
	        }

        }else{
    		Log.e(LOG_TAG, "inflater null");
    	}        
	}
	
	/**
	 * Displays map of characters to user. Called from CharacterLoader after loading has been done.
	 */
	public void displayCharacters(){
		if(mCharacters == null || mCharacters.size() < 1 || getView() == null){
			Log.w(LOG_TAG, "displayCharacters called - null");
			return ;
		}
        LinearLayout outerContainer = ((LinearLayout)getView().findViewById(R.id.translation_kanji_container));
        if(outerContainer == null){
        	Log.w(LOG_TAG, "displayCharacters called - outerContainer null");
        	return ;
        }
        Log.i(LOG_TAG, "displayCharacters called - display");
        outerContainer.setVisibility(View.VISIBLE);
        String writeCharacters = mTranslation.getJapaneseKeb().get(0);
        LinearLayout container = (LinearLayout)getView().findViewById(R.id.translation_kanji_meanings_container);
        container.removeAllViews();
        for(int i =0; i < writeCharacters.length(); i++){
        	String character = String.valueOf(writeCharacters.charAt(i));
        	JapaneseCharacter japCharacter = mCharacters.get(character);
        	if(japCharacter != null){
	        	View translationKanji = mInflater.inflate(R.layout.kanji_line, null);
	        	TextView kanjiView = (TextView) translationKanji.findViewById(R.id.translation_kanji);
	        	kanjiView.setText(character);
	        	TextView meaningView = (TextView) translationKanji.findViewById(R.id.translation_kanji_meaning);
	        	if(mEnglish && japCharacter.getMeaningEnglish() != null){
	        		int meaningSize = japCharacter.getMeaningEnglish().size();
	        		if(meaningSize > 0){
		        		int j =0;
	        			StringBuilder strBuilder = new StringBuilder();
	        			for(String str:japCharacter.getMeaningEnglish()){
	        				strBuilder.append(str);
	        				j++;
	        				if(j < meaningSize){
	        					strBuilder.append(", ");
	        				}
	        			}
	        			meaningView.setText(strBuilder);
	        			
	        		}
	        	}else if(mFrench && japCharacter.getMeaningFrench() != null){
	        		int meaningSize = japCharacter.getMeaningFrench().size();
	        		if(meaningSize > 0){
		        		int j =0;
	        			StringBuilder strBuilder = new StringBuilder();
	        			for(String str:japCharacter.getMeaningFrench()){
	        				strBuilder.append(str);
	        				j++;
	        				if(j < meaningSize){
	        					strBuilder.append(", ");
	        				}
	        			}
	        			meaningView.setText(strBuilder);
	        		}
	        	}else if(mDutch && japCharacter.getMeaningDutch() != null){
	        		int meaningSize = japCharacter.getMeaningDutch().size();
	        		if(meaningSize > 0){
		        		int j =0;
	        			StringBuilder strBuilder = new StringBuilder();
	        			for(String str:japCharacter.getMeaningDutch()){
	        				strBuilder.append(str);
	        				j++;
	        				if(j < meaningSize){
	        					strBuilder.append(", ");
	        				}
	        			}
	        			meaningView.setText(strBuilder);
	        		}
	        	}else if(mGerman && japCharacter.getMeaningGerman() != null){
	        		int meaningSize = japCharacter.getMeaningGerman().size();
	        		if(meaningSize > 0){
		        		int j =0;
	        			StringBuilder strBuilder = new StringBuilder();
	        			for(String str:japCharacter.getMeaningGerman()){
	        				strBuilder.append(str);
	        				j++;
	        				if(j < meaningSize){
	        					strBuilder.append(", ");
	        				}
	        			}
	        			meaningView.setText(strBuilder);
	        		}
	        	}else{
	        		meaningView.setText(getString(R.string.translation_kanji_no_meaning));
	        	}
	        	
	        	translationKanji.findViewById(R.id.kanji_line_id).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						TextView textView = (TextView) v.findViewById(R.id.translation_kanji);
						mCallbackTranslation.showKanjiDetail(mCharacters.get(textView.getText().toString()));
					}
				});
	        	
	        	
	        	container.addView(translationKanji);
        	}
        }

		
	}
	
	/**
	 * Sets new character info and if the fragment is visible then changes UI
	 * @param characters map of characters to be displayed
	 */
	public void setCharacters(Map<String, JapaneseCharacter> characters) {
		this.mCharacters = characters;
	    this.displayCharacters();
	}
		
	/**
	 * Updates language preferences
	 * 
	 * @return true if some language was changed
	 * 	       false if there wasn't change
	 */
    private boolean updateLanguages(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean changed = false;
        boolean englTemp = sharedPrefs.getBoolean("language_english", false);
        if(englTemp != mEnglish){
        	mEnglish = englTemp;
        	changed = true;
        }
        boolean frenchTemp = sharedPrefs.getBoolean("language_french", false);  
        if(frenchTemp != mFrench){
        	mFrench = frenchTemp;
        	changed = true;
        }
        boolean dutchTemp = sharedPrefs.getBoolean("language_dutch", false);  
        if(dutchTemp != mDutch){
        	mDutch = dutchTemp;
        	changed = true;
        }
        boolean germanTemp = sharedPrefs.getBoolean("language_german", false);  
        if(germanTemp != mGerman){
        	mGerman = germanTemp;
        	changed = true;
        }
        return changed;
    }


    public void displayNote(String note){
        LinearLayout container = (LinearLayout)getActivity().findViewById(R.id.translation_note_container);
        if(container != null){
            if(note == null || note.length() == 0){
                container.setVisibility(View.GONE);
            }else{
                container.setVisibility(View.VISIBLE);
            }
            TextView textView = (TextView) getActivity().findViewById(R.id.translation_note_text);
            if(textView != null){
                textView.setText(note);
            }
        }
    }

    public void showNoteAlertBox(String note){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.translation_note);
        builder.setCancelable(true);

        final EditText input = new EditText(getActivity());

        input.setText(note);
        input.setMinHeight(200);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String note = input.getText().toString().trim();
                mNote.setEnabled(false);
                NoteSaver noteSaver = new NoteSaver(mCallbackTranslation.getDatabse(), mNote, DisplayTranslation.this, mTranslation);
                noteSaver.execute(note);
            }
        })
        .setNegativeButton(getString(R.string.storno), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void updateMenu(){
        FavoriteLoader favoriteLoader = new FavoriteLoader(mCallbackTranslation.getDatabse(), mFavorite, this);
        favoriteLoader.execute(mTranslation);

        NoteLoader noteLoader = new NoteLoader(mCallbackTranslation.getDatabse(), mNote, this);
        noteLoader.execute(mTranslation);
        mActualMenuItems = true;

    }


}
