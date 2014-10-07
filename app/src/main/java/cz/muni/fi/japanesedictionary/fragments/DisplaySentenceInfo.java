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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.entity.TatoebaSentence;


/**
 * Fragment which displays japanese sentence info
 * @author Jaroslav Klech
 *
 */
public class DisplaySentenceInfo extends Fragment {
	
    private static final String LOG_TAG = "DisplaySentenceInfo";
	
	private TatoebaSentence mTatoebaSentence;
	private LayoutInflater mInflater;

	private boolean mEnglish;
    private boolean mFrench;        
    private boolean mDutch;
    private boolean mGerman;
    private boolean mRussian;

    public static DisplaySentenceInfo newInstance(TatoebaSentence sentence){
        DisplaySentenceInfo frag = new DisplaySentenceInfo();
        Bundle args = new Bundle();
        args.putAll(sentence.convertToBundle());
        frag.setArguments(args);
        return frag;
    }

    public static DisplaySentenceInfo newInstance(Bundle sentence){
        DisplaySentenceInfo frag = new DisplaySentenceInfo();
        frag.setArguments(sentence);
        return frag;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.display_sentence, container, false);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTatoebaSentence = TatoebaSentence.createFromBundle(getArguments());
	}



    @Override
    public void onStart() {
        super.onStart();
        updateSentence(getView());
    }

    /**
	 * Updates Fragment view acording to saved japanese character.
	 * 
	 */
	private void updateSentence(View view){
		Log.i(LOG_TAG,"Setting sentence");
		if(mTatoebaSentence == null){
			Toast.makeText(getActivity(), R.string.character_unknown_sentence, Toast.LENGTH_LONG).show();
			return;
		}
        if(view == null || getActivity() == null){
            return;
        }
		updateLanguages();
        if(mTatoebaSentence.getJapaneseSentence() != null) {
            TextView japanese = (TextView) view.findViewById(R.id.sentence_japanese);
            japanese.setText(mTatoebaSentence.getJapaneseSentence());
        }
        boolean hasMeaning = false;
        ViewGroup container = (ViewGroup) view.findViewById(R.id.tatoeba_sentences_lines_container);
		if((mEnglish || (!mDutch && !mFrench && !mGerman && !mRussian)) && mTatoebaSentence.getEnglish() != null){
			Log.i(LOG_TAG,"Setting english meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.sentence_meaning, container, false);
			TextView language = (TextView) languageView.findViewById(R.id.sentence_language);
            if (!mFrench && !mDutch && !mGerman && !mRussian) {
                language.setVisibility(View.GONE);
            }
			language.setText(R.string.language_english);

			TextView meaningTextView = (TextView)languageView.findViewById(R.id.sentence_translation);
			meaningTextView.setText(mTatoebaSentence.getEnglish());
			container.addView(languageView);
		}
		
		if(mFrench && mTatoebaSentence.getFrench() != null){
			Log.i(LOG_TAG,"Setting french meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.sentence_meaning, container, false);
			TextView language = (TextView) languageView.findViewById(R.id.sentence_language);
            if (!mEnglish && !mDutch && !mGerman && !mRussian) {
                language.setVisibility(View.GONE);
            }
			language.setText(R.string.language_french);

			TextView meaningTextView = (TextView)languageView.findViewById(R.id.sentence_translation);
			meaningTextView.setText(mTatoebaSentence.getFrench());
			container.addView(languageView);
		}

		if(mDutch && mTatoebaSentence.getDutch() != null){
			Log.i("DisplayCharacter","Setting dutch meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.sentence_meaning, container, false);
			TextView language = (TextView) languageView.findViewById(R.id.sentence_language);
            if (!mFrench && !mEnglish && !mGerman && !mRussian) {
                language.setVisibility(View.GONE);
            }
			language.setText(R.string.language_dutch);

			TextView meaningTextView = (TextView)languageView.findViewById(R.id.sentence_translation);
			meaningTextView.setText(mTatoebaSentence.getDutch());
			container.addView(languageView);
		}

		if(mGerman && mTatoebaSentence.getGerman() != null ){
			Log.i(LOG_TAG,"Setting german meaning");
			hasMeaning = true;
			View languageView = mInflater.inflate(R.layout.sentence_meaning, container, false);
			TextView language = (TextView) languageView.findViewById(R.id.sentence_language);
            if (!mFrench && !mDutch && !mEnglish && !mRussian) {
                language.setVisibility(View.GONE);
            }
			language.setText(R.string.language_german);

			TextView meaningTextView = (TextView)languageView.findViewById(R.id.sentence_translation);
			meaningTextView.setText(mTatoebaSentence.getGerman());
			container.addView(languageView);
		}

        if(mRussian && mTatoebaSentence.getRussian() != null ){
            Log.i(LOG_TAG,"Setting russian meaning");
            hasMeaning = true;
            View languageView = mInflater.inflate(R.layout.sentence_meaning, container, false);
            TextView language = (TextView) languageView.findViewById(R.id.sentence_language);
            if (!mFrench && !mDutch && !mEnglish && !mGerman) {
                language.setVisibility(View.GONE);
            }
            language.setText(R.string.language_russian);

            TextView meaningTextView = (TextView)languageView.findViewById(R.id.sentence_translation);
            meaningTextView.setText(mTatoebaSentence.getRussian());
            container.addView(languageView);
        }

		if(!hasMeaning){
			Log.i(LOG_TAG,"Doesn't have meanings");
			getView().findViewById(R.id.kanjidict_meanings_container).setVisibility(View.GONE);
		}
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
	
	
}
