package cz.muni.fi.japanesedictionary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.muni.fi.japanesedictionary.main.Languages;

public class TextFragment extends Fragment{
	public final static String ACTUAL_INDEX = "cz.muni.fi.japanesedictionary.POSITION";
	int index = -1;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		
		if(savedInstanceState != null){
			index = savedInstanceState.getInt(ACTUAL_INDEX);
		}
		
		return inflater.inflate(R.layout.article_view, container,false);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		
		Bundle args = getArguments();
		if(args != null){
			updateText(args.getInt(ACTUAL_INDEX));
		}else if(index != -1){
			updateText(index);
		}
	}
	
	
	public void updateText(int position){
		TextView article = (TextView)getActivity().findViewById(R.id.text);
		Languages languages = new Languages(this);
		article.setText(languages.getLanguage(position));
		index = position;
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ACTUAL_INDEX, index);
    }
}
