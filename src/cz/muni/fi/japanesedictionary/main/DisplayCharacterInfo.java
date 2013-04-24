package cz.muni.fi.japanesedictionary.main;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;

/**
 * Fragment which displays japanese character info
 * @author Jaroslav Klech
 *
 */
public class DisplayCharacterInfo extends SherlockFragment{
	
	private JapaneseCharacter mJapaneseCharacter;
	private LayoutInflater mInflater;

	private boolean mEnglish;
    private boolean mFrench;        
    private boolean mDutch;
    private boolean mGerman;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(mJapaneseCharacter != null){
			outState = mJapaneseCharacter.createBundleFromJapaneseCharacter(outState);
			Log.i("DisplayCharacterInfo","saving state: "+outState);
		}
		super.onSaveInstanceState(outState);
	}
	
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.display_character, null);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {		
		setHasOptionsMenu(true);
		mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(savedInstanceState != null){
			mJapaneseCharacter = JapaneseCharacter.newInstanceFromBundle(savedInstanceState);
			Log.i("DisplayCharacterInfo","saved state: "+savedInstanceState);	
		}
		super.onCreate(savedInstanceState);
	}
	

	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if(mJapaneseCharacter ==null){
			Log.i("DisplayCharacterInfo","Get japanese character from bundle");
			Bundle bundle = getArguments();
			mJapaneseCharacter = JapaneseCharacter.newInstanceFromBundle(bundle);
		}

		
	}
	
	@Override
	public void onStart() {
		if(updateLanguages()){
			updateCharacter();
		}
		super.onStart();
	}
	
	
	/**
	 * Updates Fragment view acording to saved japanese character.
	 * 
	 */
	private void updateCharacter(){
		Log.i("DisplayCharacterInfo","Setting literal");
		if(mJapaneseCharacter == null){
			Toast.makeText(getActivity(), R.string.character_unknown_character, Toast.LENGTH_LONG).show();
			return;
		}
		
		TextView literal = (TextView)getView().findViewById(R.id.kanjidict_literal);
		literal.setText(mJapaneseCharacter.getLiteral());
		getSherlockActivity().getSupportActionBar().setTitle(mJapaneseCharacter.getLiteral());
		
		if(mJapaneseCharacter.getRadicalClassic() != 0){
			Log.i("DisplayCharacterInfo","Setting radical: " + mJapaneseCharacter.getRadicalClassic());
			TextView radicalClassical = (TextView)getView().findViewById(R.id.kanjidict_radical);
			radicalClassical.setText(String.valueOf(mJapaneseCharacter.getRadicalClassic()));
		}else{
			getView().findViewById(R.id.kanjidict_radical_container).setVisibility(View.GONE);
		}
		
		if(mJapaneseCharacter.getGrade() != 0){
			Log.i("DisplayCharacterInfo","Setting grade: " + mJapaneseCharacter.getGrade());
			TextView grade = (TextView)getView().findViewById(R.id.kanjidict_grade);
			grade.setText(String.valueOf(mJapaneseCharacter.getGrade()));
		}else{
			getView().findViewById(R.id.kanjidict_grade_container).setVisibility(View.GONE);
		}
		
		if(mJapaneseCharacter.getStrokeCount() != 0){
			Log.i("DisplayCharacterInfo","Setting stroke count: " + mJapaneseCharacter.getStrokeCount());
			TextView strokeCount = (TextView)getView().findViewById(R.id.kanjidict_stroke_count);
			strokeCount.setText(String.valueOf(mJapaneseCharacter.getStrokeCount()));
		}else{
			getView().findViewById(R.id.kanjidict_stroke_count_container).setVisibility(View.GONE);
		}
		
		if(mJapaneseCharacter.getSkip() != null && mJapaneseCharacter.getSkip().length() > 0){
			Log.i("DisplayCharacterInfo","Setting skip: " + mJapaneseCharacter.getSkip());
			TextView skip = (TextView)getView().findViewById(R.id.kanjidict_skip);
			skip.setText(mJapaneseCharacter.getSkip());
		}else{
			getView().findViewById(R.id.kanjidict_skip_container).setVisibility(View.GONE);
		}
	
		
		if(mJapaneseCharacter.getNanori() != null && mJapaneseCharacter.getNanori().size() > 0){
			Log.i("DisplayCharacterInfo","Setting nanori: " + mJapaneseCharacter.getNanori());
			int count = mJapaneseCharacter.getNanori().size();
			int i =1;
			StringBuilder strBuilder = new StringBuilder();
			for(String nanori:mJapaneseCharacter.getNanori()){
				strBuilder.append(nanori);
				if(i < count){
					strBuilder.append(", ");
				}
				i++;
			}
			TextView nanori = (TextView)getView().findViewById(R.id.kanjidict_nanori);
			nanori.setText(strBuilder);
		}else{
			getView().findViewById(R.id.kanjidict_nanori_container).setVisibility(View.GONE);
		}

		if(mJapaneseCharacter.getRmGroupJaKun() != null && mJapaneseCharacter.getRmGroupJaKun().size() > 0){
			Log.i("DisplayCharacterInfo","Setting kunyomi: " + mJapaneseCharacter.getRmGroupJaKun());
			int count = mJapaneseCharacter.getRmGroupJaKun().size();
			int i =1;
			StringBuilder strBuilder = new StringBuilder();
			for(String kunyomi:mJapaneseCharacter.getRmGroupJaKun()){
				strBuilder.append(kunyomi);
				if(i < count){
					strBuilder.append(", ");
				}
				i++;
			}
			TextView kunyomi = (TextView)getView().findViewById(R.id.kanjidict_kunyomi);
			kunyomi.setText(strBuilder);
		}else{
			getView().findViewById(R.id.kanjidict_kunyomi_container).setVisibility(View.GONE);
		}
		
		if(mJapaneseCharacter.getRmGroupJaOn() != null && mJapaneseCharacter.getRmGroupJaOn().size() > 0){
			Log.i("DisplayCharacterInfo","Setting onnyomi: " + mJapaneseCharacter.getRmGroupJaOn());
			int count = mJapaneseCharacter.getRmGroupJaOn().size();
			int i =1;
			StringBuilder strBuilder = new StringBuilder();
			for(String onyomi:mJapaneseCharacter.getRmGroupJaOn()){
				strBuilder.append(onyomi);
				if(i < count){
					strBuilder.append(", ");
				}
				i++;
			}
			TextView onyomi = (TextView)getView().findViewById(R.id.kanjidict_onyomi);
			onyomi.setText(strBuilder);
		}else{
			getView().findViewById(R.id.kanjidict_onyomi_container).setVisibility(View.GONE);
		}
		
		boolean hasMeaning = false; 	
		LinearLayout container = (LinearLayout) getView().findViewById(R.id.kanjidict_meanings_lines_container);
		container.removeAllViews();
		if(mEnglish && mJapaneseCharacter.getMeaningEnglish() != null && mJapaneseCharacter.getMeaningEnglish().size() > 0){
			Log.i("DisplayCharacter","Setting english meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.kanji_meaning, null);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
			language.setText(R.string.language_english);
			int i =1;
			int count = mJapaneseCharacter.getMeaningEnglish().size();
			StringBuilder strBuilder = new StringBuilder();
			for(String meaning : mJapaneseCharacter.getMeaningEnglish()){
				strBuilder.append(meaning);
				if(i < count){
					strBuilder.append(", ");
				}
				i++;
			}
			TextView meaningTextView = (TextView)languageView.findViewById(R.id.kanjidict_translation);
			meaningTextView.setText(strBuilder);
			Log.i("DisplayCharacter","Setting english meaning: "+strBuilder);
			container.addView(languageView);
		}
		
		if(mFrench && mJapaneseCharacter.getMeaningFrench() != null && mJapaneseCharacter.getMeaningFrench().size() > 0){
			Log.i("DisplayCharacter","Setting french meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.kanji_meaning, null);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
			language.setText(R.string.language_french);
			int i =1;
			int count = mJapaneseCharacter.getMeaningFrench().size();
			StringBuilder strBuilder = new StringBuilder();
			for(String meaning : mJapaneseCharacter.getMeaningFrench()){
				strBuilder.append(meaning);
				if(i < count){
					strBuilder.append(", ");
				}
				i++;
			}
			TextView meaningTextView = (TextView)languageView.findViewById(R.id.kanjidict_translation);
			meaningTextView.setText(strBuilder);
			container.addView(languageView);
		}

		if(mDutch && mJapaneseCharacter.getMeaningDutch() != null && mJapaneseCharacter.getMeaningDutch().size() > 0){
			Log.i("DisplayCharacter","Setting dutch meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.kanji_meaning, null);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
			language.setText(R.string.language_dutch);
			int i =1;
			int count = mJapaneseCharacter.getMeaningDutch().size();
			StringBuilder strBuilder = new StringBuilder();
			for(String meaning : mJapaneseCharacter.getMeaningDutch()){
				strBuilder.append(meaning);
				if(i < count){
					strBuilder.append(", ");
				}
				i++;
			}
			TextView meaningTextView = (TextView)languageView.findViewById(R.id.kanjidict_translation);
			meaningTextView.setText(strBuilder);
			container.addView(languageView);
		}

		if(mGerman && mJapaneseCharacter.getMeaningGerman() != null && mJapaneseCharacter.getMeaningGerman().size() > 0){
			Log.i("DisplayCharacter","Setting german meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.kanji_meaning, null);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
			language.setText(R.string.language_german);
			int i =1;
			int count = mJapaneseCharacter.getMeaningGerman().size();
			StringBuilder strBuilder = new StringBuilder();
			for(String meaning : mJapaneseCharacter.getMeaningGerman()){
				strBuilder.append(meaning);
				if(i < count){
					strBuilder.append(", ");
				}
				i++;
			}
			TextView meaningTextView = (TextView)languageView.findViewById(R.id.kanjidict_translation);
			meaningTextView.setText(strBuilder);
			container.addView(languageView);
		}
		
		if(!hasMeaning){
			Log.i("DisplayCharacter","Doesn't have meanings");
			getView().findViewById(R.id.kanjidict_meanings_container).setVisibility(View.GONE);
		}

		
		if(mJapaneseCharacter.getDicRef() != null && mJapaneseCharacter.getDicRef().size() > 0){
			Log.i("DisplayCharacterInfo","Setting dictionary references");
			Map<String, String> dictionaries = getDictionaryCodes();
			LinearLayout dictionariesContainer = (LinearLayout) getView().findViewById(R.id.kanjidict_dictionaries_records);
			for(String key:mJapaneseCharacter.getDicRef().keySet()){
				View dictionaryLine = mInflater.inflate(R.layout.dictionary_line, null);
				String dictName = dictionaries.get(key);
				if(dictName != null && dictName.length() > 0){
					TextView dictNameView = (TextView)dictionaryLine.findViewById(R.id.kanjidict_dictionary_dict);
					dictNameView.setText(dictName);
					TextView dictNumber = (TextView)dictionaryLine.findViewById(R.id.kanjidict_dictionary_number);
					dictNumber.setText(mJapaneseCharacter.getDicRef().get(key));
					
					dictionariesContainer.addView(dictionaryLine);
				}
				
			}
		}else{
			getView().findViewById(R.id.kanjidict_dictionaries_container).setVisibility(View.GONE);
		}
		
		
		
	}
	
	/**
	 * Constructs map of dictionary references
	 * 
	 * @return Map<String, String> dictionary references
	 */
	public static final Map<String, String> getDictionaryCodes(){
		Map<String, String> dictionaryCodes = new HashMap<String,String>();
		dictionaryCodes.put("nelson_c", "Modern Reader's Japanese-English Character Dictionary");
		dictionaryCodes.put("nelson_n", "The New Nelson Japanese-English Character Dictionary");
		dictionaryCodes.put("halpern_njecd", "New Japanese-English Character Dictionary");
		dictionaryCodes.put("halpern_kkld", "Kanji Learners Dictionary");
		dictionaryCodes.put("heisig", "Remembering The  Kanji");
		dictionaryCodes.put("gakken", "A  New Dictionary of Kanji Usage");
		dictionaryCodes.put("oneill_names", "Japanese Names");
		dictionaryCodes.put("oneill_kk", "Essential Kanji");
		dictionaryCodes.put("moro", "Daikanwajiten");
		dictionaryCodes.put("henshall", "A Guide To Remembering Japanese Characters");
		dictionaryCodes.put("sh_kk", "Kanji and Kana");
		dictionaryCodes.put("sakade", "A Guide To Reading and Writing Japanese");
		dictionaryCodes.put("jf_cards", "Japanese Kanji Flashcards");
		dictionaryCodes.put("henshall3", "A Guide To Reading and Writing Japanese");
		dictionaryCodes.put("tutt_cards", "Tuttle Kanji Cards, compiled by Alexander Kask");
		dictionaryCodes.put("crowley", "The Kanji Way to Japanese Language Power");
		dictionaryCodes.put("kanji_in_context", "Kanji in Context");
		dictionaryCodes.put("busy_people", "Japanese For Busy People");
		dictionaryCodes.put("kodansha_compact", "Kodansha Compact Kanji Guide");
		dictionaryCodes.put("maniette", "Les Kanjis dans la tete");
		
		return dictionaryCodes;
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
