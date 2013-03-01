package cz.muni.fi.japanesedictionary.main;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.muni.fi.japanesedictionary.R;

public class DisplayTranslation extends SherlockFragment {
	
	OnCreateTranslationListener mCallbackTranslation;
	
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
			Translation translation =  mCallbackTranslation.getTranslationCallBack(index);
			updateTranslation(translation);
		}
		
		super.onCreate(savedInstanceState);
	}
	
	public void updateTranslation(Translation translation){
		Log.w("DisplayTranslation","translation: "+translation);
	}
	
}
