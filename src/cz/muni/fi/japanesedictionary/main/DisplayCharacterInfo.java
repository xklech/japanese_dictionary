package cz.muni.fi.japanesedictionary.main;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
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

public class DisplayCharacterInfo extends SherlockFragment{
	
	private JapaneseCharacter japaneseCharacter;
	private OnLoadGetCharacterListener mCallbackCharacter;
	private LayoutInflater inflater;

	private boolean english;
    private boolean french;        
    private boolean dutch;
    private boolean german;
	
    private boolean changed = false;
	
	public interface OnLoadGetCharacterListener{
		
		public JapaneseCharacter getJapaneseCharacter();
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(japaneseCharacter != null){
			outState = japaneseCharacter.createBundleFromJapaneseCharacter(outState);
			Log.i("DisplayCharacterInfo","saving state: "+outState);
		}
		super.onSaveInstanceState(outState);
	}
	
	
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	mCallbackCharacter = (OnLoadGetCharacterListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoadGetCharacterListener");
        }
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.display_character, null);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {		
		setHasOptionsMenu(true);
        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(savedInstanceState != null){
			japaneseCharacter = JapaneseCharacter.newInstanceFromBundle(savedInstanceState);
			Log.i("DisplayCharacterInfo","saved state: "+savedInstanceState);	
		}
		super.onCreate(savedInstanceState);
	}
	

	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if(savedInstanceState == null){
			if(japaneseCharacter != null){
				Log.i("DisplayCharacterInfo","Update fragment view");
			}else{
				Log.i("DisplayCharacterInfo","Construct from bundle");
				japaneseCharacter =  mCallbackCharacter.getJapaneseCharacter();
				
			}
		}

		
	}
	
	@Override
	public void onStart() {
		if(updateLanguages() || changed){
			updateCharacter();
		}
		super.onStart();
	}
	
	public void changeCharacter(JapaneseCharacter character){
		japaneseCharacter = character;
		if(this.isVisible()){
			updateCharacter();
		}
	}
	
	private void updateCharacter(){
		Log.i("DisplayCharacterInfo","Setting literal");
		changed = false;
		if(japaneseCharacter == null){
			Toast.makeText(getActivity(), R.string.tramslation_unknown_translation, Toast.LENGTH_LONG).show();
			return;
		}
		
		TextView literal = (TextView)getView().findViewById(R.id.kanjidict_literal);
		literal.setText(japaneseCharacter.getLiteral());
		
		if(japaneseCharacter.getRadicalClassic() != 0){
			Log.i("DisplayCharacterInfo","Setting radical: " + japaneseCharacter.getRadicalClassic());
			TextView radicalClassical = (TextView)getView().findViewById(R.id.kanjidict_radical);
			radicalClassical.setText(String.valueOf(japaneseCharacter.getRadicalClassic()));
		}else{
			getView().findViewById(R.id.kanjidict_radical_container).setVisibility(View.GONE);
		}
		
		if(japaneseCharacter.getGrade() != 0){
			Log.i("DisplayCharacterInfo","Setting grade: " + japaneseCharacter.getGrade());
			TextView grade = (TextView)getView().findViewById(R.id.kanjidict_grade);
			grade.setText(String.valueOf(japaneseCharacter.getGrade()));
		}else{
			getView().findViewById(R.id.kanjidict_grade_container).setVisibility(View.GONE);
		}
		
		if(japaneseCharacter.getStrokeCount() != 0){
			Log.i("DisplayCharacterInfo","Setting stroke count: " + japaneseCharacter.getStrokeCount());
			TextView strokeCount = (TextView)getView().findViewById(R.id.kanjidict_stroke_count);
			strokeCount.setText(String.valueOf(japaneseCharacter.getStrokeCount()));
		}else{
			getView().findViewById(R.id.kanjidict_stroke_count_container).setVisibility(View.GONE);
		}
		
		if(japaneseCharacter.getSkip() != null && japaneseCharacter.getSkip().length() > 0){
			Log.i("DisplayCharacterInfo","Setting skip: " + japaneseCharacter.getSkip());
			TextView skip = (TextView)getView().findViewById(R.id.kanjidict_skip);
			skip.setText(japaneseCharacter.getSkip());
		}else{
			getView().findViewById(R.id.kanjidict_skip_container).setVisibility(View.GONE);
		}
	
		
		if(japaneseCharacter.getNanori() != null && japaneseCharacter.getNanori().size() > 0){
			Log.i("DisplayCharacterInfo","Setting nanori: " + japaneseCharacter.getNanori());
			int count = japaneseCharacter.getNanori().size();
			int i =1;
			StringBuilder strBuilder = new StringBuilder();
			for(String nanori:japaneseCharacter.getNanori()){
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

		if(japaneseCharacter.getRmGroupJaKun() != null && japaneseCharacter.getRmGroupJaKun().size() > 0){
			Log.i("DisplayCharacterInfo","Setting kunyomi: " + japaneseCharacter.getRmGroupJaKun());
			int count = japaneseCharacter.getRmGroupJaKun().size();
			int i =1;
			StringBuilder strBuilder = new StringBuilder();
			for(String kunyomi:japaneseCharacter.getRmGroupJaKun()){
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
		
		if(japaneseCharacter.getRmGroupJaOn() != null && japaneseCharacter.getRmGroupJaOn().size() > 0){
			Log.i("DisplayCharacterInfo","Setting onnyomi: " + japaneseCharacter.getRmGroupJaOn());
			int count = japaneseCharacter.getRmGroupJaOn().size();
			int i =1;
			StringBuilder strBuilder = new StringBuilder();
			for(String onyomi:japaneseCharacter.getRmGroupJaOn()){
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
		if(english && japaneseCharacter.getMeaningEnglish() != null && japaneseCharacter.getMeaningEnglish().size() > 0){
			Log.i("DisplayCharacter","Setting english meaning");
			hasMeaning = true;
			View languageView = inflater.inflate(R.layout.kanji_meaning, null);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
			language.setText(R.string.language_english);
			int i =1;
			int count = japaneseCharacter.getMeaningEnglish().size();
			StringBuilder strBuilder = new StringBuilder();
			for(String meaning : japaneseCharacter.getMeaningEnglish()){
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
		
		if(french && japaneseCharacter.getMeaningFrench() != null && japaneseCharacter.getMeaningFrench().size() > 0){
			Log.i("DisplayCharacter","Setting french meaning");
			hasMeaning = true;
			View languageView = inflater.inflate(R.layout.kanji_meaning, null);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
			language.setText(R.string.language_french);
			int i =1;
			int count = japaneseCharacter.getMeaningFrench().size();
			StringBuilder strBuilder = new StringBuilder();
			for(String meaning : japaneseCharacter.getMeaningFrench()){
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

		if(dutch && japaneseCharacter.getMeaningDutch() != null && japaneseCharacter.getMeaningDutch().size() > 0){
			Log.i("DisplayCharacter","Setting dutch meaning");
			hasMeaning = true;
			View languageView = inflater.inflate(R.layout.kanji_meaning, null);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
			language.setText(R.string.language_dutch);
			int i =1;
			int count = japaneseCharacter.getMeaningDutch().size();
			StringBuilder strBuilder = new StringBuilder();
			for(String meaning : japaneseCharacter.getMeaningDutch()){
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

		if(german && japaneseCharacter.getMeaningGerman() != null && japaneseCharacter.getMeaningGerman().size() > 0){
			Log.i("DisplayCharacter","Setting german meaning");
			hasMeaning = true;
			View languageView = inflater.inflate(R.layout.kanji_meaning, null);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
			language.setText(R.string.language_german);
			int i =1;
			int count = japaneseCharacter.getMeaningGerman().size();
			StringBuilder strBuilder = new StringBuilder();
			for(String meaning : japaneseCharacter.getMeaningGerman()){
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

		
		if(japaneseCharacter.getDicRef() != null && japaneseCharacter.getDicRef().size() > 0){
			Log.i("DisplayCharacterInfo","Setting dictionary references");
			Map<String, String> dictionaries = getDictionaryCodes();
			LinearLayout dictionariesContainer = (LinearLayout) getView().findViewById(R.id.kanjidict_dictionaries_records);
			for(String key:japaneseCharacter.getDicRef().keySet()){
				View dictionaryLine = inflater.inflate(R.layout.dictionary_line, null);
				String dictName = dictionaries.get(key);
				if(dictName != null && dictName.length() > 0){
					TextView dictNameView = (TextView)dictionaryLine.findViewById(R.id.kanjidict_dictionary_dict);
					dictNameView.setText(dictName);
					TextView dictNumber = (TextView)dictionaryLine.findViewById(R.id.kanjidict_dictionary_number);
					dictNumber.setText(japaneseCharacter.getDicRef().get(key));
					
					dictionariesContainer.addView(dictionaryLine);
				}
				
			}
		}else{
			getView().findViewById(R.id.kanjidict_dictionaries_container).setVisibility(View.GONE);
		}
		
		
		
	}
	
	
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
