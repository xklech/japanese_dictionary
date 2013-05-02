package cz.muni.fi.japanesedictionary.fragments;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.database.DBAsyncTask;
import cz.muni.fi.japanesedictionary.engine.CharacterLoader;
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
	
	private OnCreateTranslationListener mCallbackTranslation;
	private Translation mTranslation = null;
	private Map<String, JapaneseCharacter> mCharacters = null;
	private LayoutInflater mInflater = null;
	


	private boolean mEnglish;
    private boolean mFrench;        
    private boolean mDutch;
    private boolean mGerman;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	mCallbackTranslation = (OnCreateTranslationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.display_translation, null);
	}

	@Override
	public void onStart() {
		updateTranslation();
		super.onStart();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState != null){
			mTranslation = Translation.newInstanceFromBundle(savedInstanceState);
			Log.i("DisplayTranslation","Loading from saved state, restoring translation");
		}

		setHasOptionsMenu(true);
		mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		if(mTranslation!= null){
			outState = mTranslation.createBundleFromTranslation(outState);
		}
		Log.i("DisplayTranslation","Saving instance ");
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
		if(this.isVisible()){
			updateTranslation();
		}
	}
	
	/**
	 * Updates Fragment view acording to saved translation.
	 * 
	 */
	public void updateTranslation(){
		Log.i("DisplayTranslation","Update translation");
		
		LinearLayout layoutSelect = (LinearLayout) getView().findViewById(R.id.translation_select);
		LinearLayout layoutTranslation = (LinearLayout) getView().findViewById(R.id.translation_container);
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
	       	((LinearLayout)getView().findViewById(R.id.translation_alternative_container)).setVisibility(View.VISIBLE);
		}else{
			((LinearLayout)getView().findViewById(R.id.translation_alternative_container)).setVisibility(View.GONE);
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
	        ((LinearLayout)getView().findViewById(R.id.translation_kanji_container)).setVisibility(View.GONE);
	        mCharacters = null;
	        if(writeCharacters != null){
	        	// write single characters
	        	if(writeCharacters.length() > 0){
	        		CharacterLoader charLoader  = new CharacterLoader(getActivity().getApplicationContext(),this);
	        		charLoader.execute(writeCharacters);
	        	}
	        }
	        
	        
    	}else{
    		Log.e("DisplayTranslation", "inflater null");
    	}        
	}
	
	/**
	 * Displays map of characters to user. Called from CharacterLoader after loading has been done.
	 */
	public void displayCharacters(){
		if(mCharacters == null || mCharacters.size() < 1 || getView() == null){
			Log.w("DisplayTranslation", "displayCharacters called - null");
			return ;
		}
        LinearLayout outerContainer = ((LinearLayout)getView().findViewById(R.id.translation_kanji_container));
        if(outerContainer == null){
        	Log.w("DisplayTranslation", "displayCharacters called - outerContainer null");
        	return ;
        }
        Log.i("DisplayTranslation", "displayCharacters called - display");
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
	        		meaningView.setText(getString(R.string.tramslation_kanji_no_meaning));
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
    
    
}
