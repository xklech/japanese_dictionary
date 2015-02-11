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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.engine.KanjiStrokeLoader;
import cz.muni.fi.japanesedictionary.entity.JapaneseCharacter;
import cz.muni.fi.japanesedictionary.interfaces.KanjiVgCallback;
import cz.muni.fi.japanesedictionary.util.MiscellaneousUtil;

/**
 * Fragment which displays japanese character info
 * @author Jaroslav Klech
 *
 */
public class DisplayCharacterInfo extends Fragment implements KanjiVgCallback {
	
    private static final String LOG_TAG = "DisplayCharacterInfo";
	
	private JapaneseCharacter mJapaneseCharacter;
	private LayoutInflater mInflater;

	private boolean mEnglish;
    private boolean mFrench;        
    private boolean mDutch;
    private boolean mGerman;
    private boolean mRussian;
	
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.display_character, container, false);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mJapaneseCharacter = JapaneseCharacter.newInstanceFromBundle(getArguments());
	}

	@Override
	public void onStart() {
        super.onStart();
        updateCharacter(getView());
	}

    @Override
    public void onStop() {
        super.onStop();
        mTimerHandler.removeCallbacks(mTimerRunnable);
    }

    /**
	 * Updates Fragment view acording to saved japanese character.
	 * 
	 */
	private void updateCharacter(View view){
		Log.i(LOG_TAG,"Setting literal");
		if(mJapaneseCharacter == null){
			Toast.makeText(getActivity(), R.string.character_unknown_character, Toast.LENGTH_LONG).show();
			return;
		}


		updateLanguages();

        if(view == null || getActivity() == null){
            return;
        }

        KanjiStrokeLoader kanjiVgLoader = new KanjiStrokeLoader(getActivity().getApplicationContext(), this);
        kanjiVgLoader.execute(mJapaneseCharacter.getLiteral());


		TextView literal = (TextView)view.findViewById(R.id.kanjidict_literal);
		literal.setText(mJapaneseCharacter.getLiteral());
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(mJapaneseCharacter.getLiteral());
		
		if(mJapaneseCharacter.getRadicalClassic() != 0){
			Log.i(LOG_TAG,"Setting radical: " + mJapaneseCharacter.getRadicalClassic());
			TextView radicalClassical = (TextView)view.findViewById(R.id.kanjidict_radical);
			radicalClassical.setText(String.valueOf(mJapaneseCharacter.getRadicalClassic()));
		}else{
            view.findViewById(R.id.kanjidict_radical_container).setVisibility(View.GONE);
		}
		
		if(mJapaneseCharacter.getGrade() != 0){
			Log.i(LOG_TAG,"Setting grade: " + mJapaneseCharacter.getGrade());
			TextView grade = (TextView)view.findViewById(R.id.kanjidict_grade);
			grade.setText(String.valueOf(mJapaneseCharacter.getGrade()));
		}else{
            view.findViewById(R.id.kanjidict_grade_container).setVisibility(View.GONE);
		}
		
		if(mJapaneseCharacter.getStrokeCount() != 0){
			Log.i(LOG_TAG,"Setting stroke count: " + mJapaneseCharacter.getStrokeCount());
			TextView strokeCount = (TextView)view.findViewById(R.id.kanjidict_stroke_count);
			strokeCount.setText(String.valueOf(mJapaneseCharacter.getStrokeCount()));
		}else{
            view.findViewById(R.id.kanjidict_stroke_count_container).setVisibility(View.GONE);
		}
		
		if(mJapaneseCharacter.getSkip() != null && mJapaneseCharacter.getSkip().length() > 0){
			Log.i(LOG_TAG,"Setting skip: " + mJapaneseCharacter.getSkip());
			TextView skip = (TextView)view.findViewById(R.id.kanjidict_skip);
			skip.setText(mJapaneseCharacter.getSkip());
		}else{
            view.findViewById(R.id.kanjidict_skip_container).setVisibility(View.GONE);
		}

		if(mJapaneseCharacter.getRmGroupJaKun() != null && mJapaneseCharacter.getRmGroupJaKun().size() > 0){
			Log.i(LOG_TAG,"Setting kunyomi: " + mJapaneseCharacter.getRmGroupJaKun());
			TextView kunyomi = (TextView)view.findViewById(R.id.kanjidict_kunyomi);
			kunyomi.setText(MiscellaneousUtil.processKunyomi(getActivity(), mJapaneseCharacter.getRmGroupJaKun()));
		}else{
            view.findViewById(R.id.kanjidict_kunyomi_container).setVisibility(View.GONE);
		}
		
		if(mJapaneseCharacter.getRmGroupJaOn() != null && mJapaneseCharacter.getRmGroupJaOn().size() > 0){
			Log.i(LOG_TAG,"Setting onnyomi: " + mJapaneseCharacter.getRmGroupJaOn());
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
			TextView onyomi = (TextView)view.findViewById(R.id.kanjidict_onyomi);
			onyomi.setText(strBuilder);
		}else{
            view.findViewById(R.id.kanjidict_onyomi_container).setVisibility(View.GONE);
		}

        if(mJapaneseCharacter.getNanori() != null && mJapaneseCharacter.getNanori().size() > 0){
            Log.i(LOG_TAG,"Setting nanori: " + mJapaneseCharacter.getNanori());
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
            TextView nanori = (TextView)view.findViewById(R.id.kanjidict_nanori);
            nanori.setText(strBuilder);
        }else{
            view.findViewById(R.id.kanjidict_nanori_container).setVisibility(View.GONE);
        }
		
		boolean hasMeaning = false; 	
		LinearLayout container = (LinearLayout) view.findViewById(R.id.kanjidict_meanings_lines_container);
		container.removeAllViews();
		if((mEnglish || (!mDutch && !mFrench && !mGerman && !mRussian)) && mJapaneseCharacter.getMeaningEnglish() != null && mJapaneseCharacter.getMeaningEnglish().size() > 0){
			Log.i(LOG_TAG,"Setting english meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.kanji_meaning, container, false);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
            if (!mFrench && !mDutch && !mGerman && !mRussian) {
                language.setVisibility(View.GONE);
            }
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
			Log.i(LOG_TAG,"Setting english meaning: "+strBuilder);
			container.addView(languageView);
		}
		
		if(mFrench && mJapaneseCharacter.getMeaningFrench() != null && mJapaneseCharacter.getMeaningFrench().size() > 0){
			Log.i(LOG_TAG,"Setting french meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.kanji_meaning, container, false);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
            if (!mEnglish && !mDutch && !mGerman && !mRussian) {
                language.setVisibility(View.GONE);
            }
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
			View languageView = mInflater.inflate(R.layout.kanji_meaning, container, false);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
            if (!mFrench && !mEnglish && !mGerman && !mRussian) {
                language.setVisibility(View.GONE);
            }
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
			Log.i(LOG_TAG,"Setting german meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.kanji_meaning, container, false);
			TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
            if (!mFrench && !mDutch && !mEnglish && !mRussian) {
                language.setVisibility(View.GONE);
            }
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

        if(mRussian && mJapaneseCharacter.getMeaningRussian() != null && mJapaneseCharacter.getMeaningRussian().size() > 0){
            Log.i(LOG_TAG,"Setting russian meaning");
            hasMeaning = true;
            View languageView = mInflater.inflate(R.layout.kanji_meaning, container, false);
            TextView language = (TextView) languageView.findViewById(R.id.kanjidict_language);
            if (!mFrench && !mDutch && !mEnglish && !mGerman) {
                language.setVisibility(View.GONE);
            }
            language.setText(R.string.language_russian);
            int i =1;
            int count = mJapaneseCharacter.getMeaningRussian().size();
            StringBuilder strBuilder = new StringBuilder();
            for(String meaning : mJapaneseCharacter.getMeaningRussian()){
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
			Log.i(LOG_TAG,"Doesn't have meanings");
            view.findViewById(R.id.kanjidict_meanings_container).setVisibility(View.GONE);
		}

		
		if(mJapaneseCharacter.getDicRef() != null && mJapaneseCharacter.getDicRef().size() > 0){
			Log.i(LOG_TAG,"Setting dictionary references");
			Map<String, String> dictionaries = getDictionaryCodes();
			LinearLayout dictionariesContainer = (LinearLayout) view.findViewById(R.id.kanjidict_dictionaries_records);
            dictionariesContainer.removeAllViews();
			for(String key:mJapaneseCharacter.getDicRef().keySet()){
				View dictionaryLine = mInflater.inflate(R.layout.dictionary_line, dictionariesContainer, false);
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
            view.findViewById(R.id.kanjidict_dictionaries_container).setVisibility(View.GONE);
		}
		
		
		
	}


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateCharacter(getView());
    }

    /**
	 * Constructs map of dictionary references
	 * 
	 * @return dictionary references
	 */
	public static Map<String, String> getDictionaryCodes(){
		Map<String, String> dictionaryCodes = new HashMap<>();
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
        boolean russianTemp = sharedPrefs.getBoolean("language_russian", false);
        if(russianTemp != mRussian){
            mRussian = russianTemp;
            changed = true;
        }
        return changed;
    }

    private int mStep;
    private Handler mTimerHandler = new Handler();
    final Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if(mSvgs == null ||mImageView == null){
                return;
            }
            if(mStep > mSvgs.size() - 1){
                mStep = 0;
            }
            mImageView.setSVG(mSvgs.get(mStep));
            mStep++;
            mTimerHandler.postDelayed(this, 2000);
        }
    };
    private List<SVG> mSvgs;
    private SVGImageView mImageView;

    @Override
    public void kanjiVgLoaded(final List<SVG> svgs) {
        if(svgs == null || svgs.size() == 0){
            Log.d(LOG_TAG, "svg is null");
            return ;
        }
        if(getView() == null){
            Log.d(LOG_TAG, "view is null");
            return ;
        }
        if(getActivity() == null){
            Log.d(LOG_TAG, "activity is null");
            return ;
        }
        mSvgs = svgs;
        LinearLayout container = (LinearLayout) getView().findViewById(R.id.kanjidict_kanjivg_image_container);
        container.removeAllViews();
/*
        int minSize;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            minSize = Math.min(width/2, height/2);
        }else {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            int width = display.getWidth();  // deprecated
            int height = display.getHeight();  // deprecated
            minSize = Math.min(width/2, height/2);
        }*/
        Resources r = getResources();
        int minSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, r.getDisplayMetrics()));


        mImageView = new SVGImageView(getActivity());
        mImageView.setMinimumHeight(minSize);
        mImageView.setMinimumWidth(minSize);
        mImageView.setSVG(mSvgs.get(mStep > mSvgs.size()-1 ? 0 : mStep));
        container.addView(mImageView,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mTimerHandler.removeCallbacks(mTimerRunnable);
        mTimerHandler.postDelayed(mTimerRunnable, 0);
    }

}
