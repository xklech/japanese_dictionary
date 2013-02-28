package cz.muni.fi.japanesedictionary.main;


import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DictionaryLanguageList extends ListFragment {
	OnTextSelectedListener callBack;
	
	public interface OnTextSelectedListener{
			public void onTextSelected(int index);
	}
		
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;
        Languages languages = new Languages(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), layout,languages.getLanguages());
        setListAdapter(adapter);
        
    }
    
    @Override
    public void onAttach(Activity activity){
    	super.onAttach(activity);
    	try{
    		callBack = (OnTextSelectedListener)activity;
    	}catch(ClassCastException e){
    		throw new ClassCastException(activity.toString()
                    + " must implement OnTextSelectedListener");
    	}
    }
    
    @Override
    public void onListItemClick(ListView l,View v, int position,long id){
    	callBack.onTextSelected(position);
    }
}
