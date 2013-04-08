package cz.muni.fi.japanesedictionary.main;

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
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.parser.RomanizationEnum;

public class DisplayTranslation extends SherlockFragment {
	
	private OnCreateTranslationListener mCallbackTranslation;
	private Translation translation = null;
	private Map<String, JapaneseCharacter> characters = null;
	private LayoutInflater inflater = null;
	


	private boolean english;
    private boolean french;        
    private boolean dutch;
    private boolean german;
    
    
    
	public interface OnCreateTranslationListener{
		
		public Translation getTranslationCallBack(int index);
		
		public void showKanjiDetail(JapaneseCharacter character);
	}
	
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
			translation = Translation.newInstanceFromBundle(savedInstanceState);
			Log.i("DisplayTranslation","saved state: "+savedInstanceState);	
		}

		setHasOptionsMenu(true);
        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		if(translation!= null){
			outState = translation.createBundleFromTranslation(outState);
		}
		Log.i("DisplayTranslation","Save instance "+outState);
		super.onSaveInstanceState(outState);
	}	
	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		if(savedInstanceState != null){
			Log.w("DisplayTranslation","rotace"+savedInstanceState);
		}else{
			Bundle bundle = getArguments();
			if(bundle != null){
				int index = bundle.getInt("TranslationId");
				translation =  mCallbackTranslation.getTranslationCallBack(index);
			}
		}
		
		super.onViewCreated(view, savedInstanceState);
	}
	
	public void setTranslation(Translation tran){
		this.translation = tran;
	}
	
	
	public void updateTranslation(){
		Log.i("DisplayTranslation","update translation");
		
		LinearLayout layoutSelect = (LinearLayout) getView().findViewById(R.id.translation_select);
		LinearLayout layoutTranslation = (LinearLayout) getView().findViewById(R.id.translation_container);
		if(translation == null){
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
        if(translation.getJapaneseReb() != null){
        	String reading = translation.getJapaneseReb().get(0);
        	read.setText(reading);
        	DBAsyncTask saveTranslation   = new DBAsyncTask(((MainActivity)getActivity()).getDatabse());
        	if(saveTranslation != null){
        		saveTranslation.execute(translation);
        	}
        	TextView romaji = (TextView)getView().findViewById(R.id.translation_romaji);
        	romaji.setText(RomanizationEnum.Hepburn.toRomaji(reading));

        	
        }
        if(translation.getJapaneseKeb() != null){
        	int size_keb = translation.getJapaneseKeb().size();
        	if(size_keb > 1){
        		StringBuilder strBuilder = new StringBuilder();
        		for(int i = 1;i < size_keb;i++){
        			strBuilder.append(translation.getJapaneseKeb().get(i));
        			if(i+1 < size_keb){
        				strBuilder.append(", ");
        			}
        		}
            	alternative.setText(strBuilder);  
               	((LinearLayout)getView().findViewById(R.id.translation_alternative_container)).setVisibility(View.VISIBLE);
        	}else{
        		((LinearLayout)getView().findViewById(R.id.translation_alternative_container)).setVisibility(View.GONE);
        	}
        }
        String writeCharacters = null;
        if(translation.getJapaneseKeb() != null && translation.getJapaneseKeb().size() > 0){
        	writeCharacters = translation.getJapaneseKeb().get(0);
        	write.setText(writeCharacters);
        	write.setVisibility(View.VISIBLE);
        }else{
        	write.setVisibility(View.GONE);
        }
        

    	if(inflater != null){
    		LinearLayout translations_container = (LinearLayout)getView().findViewById(R.id.translation_translation_container);
        	translations_container.removeAllViews();
    		if((english || (!english && !french && !dutch && ! german)) && translation.getEnglishSense()!= null && translation.getEnglishSense().size() > 0){
		        	View translation_language = inflater.inflate(R.layout.translation_language, null);
	    			TextView textView = (TextView) translation_language.findViewById(R.id.translation_language);
	    			textView.setText(getString(R.string.language_english));
	    			translations_container.addView(translation_language);
		        	for(List<String> tran: translation.getEnglishSense()){
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
		        			View translation_ll = inflater.inflate(R.layout.translation_line, null);
		        			TextView tView = (TextView) translation_ll.findViewById(R.id.translation_translation);
		        			tView.setText(strBuilder.toString());
		        			translations_container.addView(translation_ll);
		        		}
		        		
		        	}
	        }
	        if(french && translation.getFrenchSense()!= null && translation.getFrenchSense().size() > 0){
	        	View translation_language = inflater.inflate(R.layout.translation_language, null);
    			TextView textView = (TextView) translation_language.findViewById(R.id.translation_language);
    			textView.setText(getString(R.string.language_french));
    			translations_container.addView(translation_language);
		        	for(List<String> tran: translation.getFrenchSense()){
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
		        			View translation_ll = inflater.inflate(R.layout.translation_line, null);
		        			TextView tView = (TextView) translation_ll.findViewById(R.id.translation_translation);
		        			tView.setText(strBuilder.toString());
		        			translations_container.addView(translation_ll);
		        		}
		        	}
	        }
	        if(dutch && translation.getDutchSense()!= null && translation.getDutchSense().size() > 0){
	        	View translation_language = inflater.inflate(R.layout.translation_language, null);
    			TextView textView = (TextView) translation_language.findViewById(R.id.translation_language);
    			textView.setText(getString(R.string.language_dutch));
    			translations_container.addView(translation_language);
		        	for(List<String> tran: translation.getDutchSense()){
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
		        			View translation_ll = inflater.inflate(R.layout.translation_line, null);
		        			TextView tView = (TextView) translation_ll.findViewById(R.id.translation_translation);
		        			tView.setText(strBuilder.toString());
		        			translations_container.addView(translation_ll);
		        		}
		        	}
	        }
	        if(german && translation.getGermanSense() != null && translation.getGermanSense().size() > 0){
	        	View translation_language = inflater.inflate(R.layout.translation_language, null);
    			TextView textView = (TextView) translation_language.findViewById(R.id.translation_language);
    			textView.setText(getString(R.string.language_german));
    			translations_container.addView(translation_language);
		        	for(List<String> tran: translation.getGermanSense()){
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
		        			View translation_ll = inflater.inflate(R.layout.translation_line, null);
		        			TextView tView = (TextView) translation_ll.findViewById(R.id.translation_translation);
		        			tView.setText(strBuilder.toString());
		        			translations_container.addView(translation_ll);
		        		}
		        	}
	        }
	        ((LinearLayout)getView().findViewById(R.id.translation_kanji_container)).setVisibility(View.GONE);
	        characters = null;
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
	
	public void displayCharacters(){
		if(characters == null || characters.size() < 1 || getView() == null){
			return ;
		}
        LinearLayout outerContainer = ((LinearLayout)getView().findViewById(R.id.translation_kanji_container));
        if(outerContainer == null){
        	return ;
        }
        outerContainer.setVisibility(View.VISIBLE);
        String writeCharacters = translation.getJapaneseKeb().get(0);
        LinearLayout container = (LinearLayout)getView().findViewById(R.id.translation_kanji_meanings_container);
        container.removeAllViews();
        for(int i =0; i < writeCharacters.length(); i++){
        	String character = String.valueOf(writeCharacters.charAt(i));
        	JapaneseCharacter japCharacter = characters.get(character);
        	if(japCharacter != null){
	        	View translationKanji = inflater.inflate(R.layout.kanji_line, null);
	        	TextView kanjiView = (TextView) translationKanji.findViewById(R.id.translation_kanji);
	        	kanjiView.setText(character);
	        	TextView meaningView = (TextView) translationKanji.findViewById(R.id.translation_kanji_meaning);
	        	if(english && japCharacter.getMeaningEnglish() != null){
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
	        	}else if(french && japCharacter.getMeaningFrench() != null){
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
	        	}else if(french && japCharacter.getMeaningDutch() != null){
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
	        	}else if(german && japCharacter.getMeaningGerman() != null){
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
						System.out.println("clicked: "+characters);
						System.out.println("clicked: "+textView.getText());
						mCallbackTranslation.showKanjiDetail(characters.get(textView.getText().toString()));
					}
				});
	        	
	        	
	        	container.addView(translationKanji);
        	}
        }
        
		
		
	}
	
	
	public void setCharacters(Map<String, JapaneseCharacter> characters) {
		this.characters = characters;
	}
		
    private boolean updateLanguages(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean changed = false;
        boolean englTemp = sharedPrefs.getBoolean("language_english", false);
        if(englTemp != english){
        	english = englTemp;
        	changed = true;
        }
        boolean frenchTemp = sharedPrefs.getBoolean("language_french", false);  
        if(frenchTemp != french){
        	french = frenchTemp;
        	changed = true;
        }
        boolean dutchTemp = sharedPrefs.getBoolean("language_dutch", false);  
        if(dutchTemp != dutch){
        	dutch = dutchTemp;
        	changed = true;
        }
        boolean germanTemp = sharedPrefs.getBoolean("language_german", false);  
        if(germanTemp != german){
        	german = germanTemp;
        	changed = true;
        }
        return changed;
    }
    
    
}
