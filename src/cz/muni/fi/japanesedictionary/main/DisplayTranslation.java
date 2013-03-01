package cz.muni.fi.japanesedictionary.main;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.japanesedictionary.R;

public class DisplayTranslation extends SherlockFragment {
	
	OnCreateTranslationListener mCallbackTranslation;
	Translation translation = null;
    LayoutInflater inflater = null;
	public interface OnCreateTranslationListener{
		public Translation getTranslationCallBack(int index);
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
	public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState != null){
			return;
		}
		Bundle bundle = getArguments();
		if(bundle != null){
			int index = bundle.getInt("TranslationId");
			translation =  mCallbackTranslation.getTranslationCallBack(index);
		}
		setHasOptionsMenu(true);
        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		updateTranslation();
		
		super.onViewCreated(view, savedInstanceState);
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.tramslation_title));
		super.onCreateOptionsMenu(menu, inflater);
	}
	public void setTranslation(Translation tran){
		this.translation = tran;
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
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void updateTranslation(){
		Log.w("DisplayTranslation","translation: "+translation);
		
		if(translation == null){
			Toast.makeText(getActivity(), R.string.tramslation_unknown_translation, Toast.LENGTH_LONG).show();
			return;
		}
		
		TextView read = (TextView)getView().findViewById(R.id.translation_read);
		TextView write = (TextView)getView().findViewById(R.id.translation_write);
		TextView alternative = (TextView)getView().findViewById(R.id.translation_alternative);
        if(translation.getJapaneseReb() != null){
        	read.setText(translation.getJapaneseReb().get(0));
        	/*int size_reb = translation.getJapaneseReb().size();
        	if(size_reb > 1){
        		StringBuilder strBuilder = new StringBuilder();
        		for(int i = 1;i < size_reb;i++){
        			strBuilder.append(translation.getJapaneseReb().get(i));
        			if(i+1 < size_reb){
        				strBuilder.append(", ");
        			}
        		}
            	alternative.setText(strBuilder);
               	((LinearLayout)getView().findViewById(R.id.translation_alternative_container)).setVisibility(View.VISIBLE);
        	}else{
        		((LinearLayout)getView().findViewById(R.id.translation_alternative_container)).setVisibility(View.GONE);
        	}*/
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
        
        if(translation.getJapaneseKeb() != null){
        	write.setText(translation.getJapaneseKeb().get(0));
        	write.setVisibility(View.VISIBLE);
        }else{
        	write.setVisibility(View.GONE);
        }
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean english = sharedPrefs.getBoolean("language_english", false);
        boolean french = sharedPrefs.getBoolean("language_french", false);        
        boolean dutch = sharedPrefs.getBoolean("language_dutch", false);
        boolean german = sharedPrefs.getBoolean("language_german", false);
    	if(inflater != null){
    		LinearLayout translations_container = (LinearLayout)getView().findViewById(R.id.translation_translation_container);
        	translations_container.removeAllViews();
    		if(english && translation.getEnglishSense().size() > 0){
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
	        if(french && translation.getFrenchSense().size() > 0){
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
	        if(dutch && translation.getDutchSense().size() > 0){
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
	        if(german && translation.getGermanSense().size() > 0){
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
    	}else{
    		Log.e("DisplayTranslation", "inflater null");
    	}        
	}
	
}
