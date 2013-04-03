package cz.muni.fi.japanesedictionary.main;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.japanesedictionary.entity.JapaneseCharacter;

public class DisplayCharacterInfo extends SherlockFragment{
	
	private JapaneseCharacter japaneseCharacter;
	private OnLoadGetCharacterListener mCallbackCharacter;
	private LayoutInflater inflater;

	
	
	
	public interface OnLoadGetCharacterListener{
		
		public JapaneseCharacter getJapaneseCharacter();
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("saved", true);
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
		japaneseCharacter =  mCallbackCharacter.getJapaneseCharacter();
		setHasOptionsMenu(true);
        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_details, menu);
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.character_title));
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if(savedInstanceState == null){
			if(japaneseCharacter != null){
				Log.i("DisplayCharacterInfo","Update fragment view");
				updateCharacter();
			}else{
				Log.i("DisplayCharacterInfo","Nothing to display");
				//TODO
				
			}
		}
		super.onViewCreated(view, savedInstanceState);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(getActivity(), MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.settings:
    			Log.i("MainActivity", "Lauching preference Activity");
    			Intent intentSetting = new Intent(getActivity().getApplicationContext(),cz.muni.fi.japanesedictionary.main.MyPreferencesActivity.class);
    			intentSetting.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(intentSetting);
    			return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	   
	
	
	private void updateCharacter(){
		Log.i("DisplayCharacterInfo","Setting literal");
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
			Log.i("DisplayCharacterInfo","Setting nanori: " + japaneseCharacter.getSkip());
			int count = japaneseCharacter.getNanori().size();
			int i =1;
			StringBuilder strBuilder = new StringBuilder();
			for(String nanori:japaneseCharacter.getNanori()){
				strBuilder.append(nanori);
				if(i < count){
					strBuilder.append(',');
				}
				i++;
			}
			TextView nanori = (TextView)getView().findViewById(R.id.kanjidict_nanori);
			nanori.setText(strBuilder);
		}else{
			getView().findViewById(R.id.kanjidict_nanori_container).setVisibility(View.GONE);
		}
		
		if(japaneseCharacter.getDicRef() != null && japaneseCharacter.getDicRef().size() > 0){
			Log.i("DisplayCharacterInfo","Setting dictionary references");
			Map<String, String> dictionaries = getDictionaryCodes();
			LinearLayout dictionariesContainer = (LinearLayout) getView().findViewById(R.id.kanjidict_dictionaries_records);
			int i =0;
			for(String key:japaneseCharacter.getDicRef().keySet()){
				i++;
				View dictionaryLine = inflater.inflate(R.layout.dictionary_line, null);
				String dictName = dictionaries.get(key);
				if(dictName != null && dictName.length() > 0){
					TextView dictNameView = (TextView)dictionaryLine.findViewById(R.id.kanjidict_dictionary_dict);
					dictNameView.setText(dictName);
					dictNameView.setId(i);
					TextView dictNumber = (TextView)dictionaryLine.findViewById(R.id.kanjidict_dictionary_number);
					dictNumber.setText(japaneseCharacter.getDicRef().get(key));
					dictNumber.setId(i*100);
					
					dictionariesContainer.addView(dictionaryLine);
				}
				
			}
		}else{
			getView().findViewById(R.id.kanjidict_dictionaries_container).setVisibility(View.GONE);
		}
		
		
		
	}
	
	
	private Map<String, String> getDictionaryCodes(){
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
	
}
