package cz.muni.fi.japanesedictionary.main;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TranslationsAdapter extends ArrayAdapter<Translation>{
    
    private Context context;

    public TranslationsAdapter(Context cont) {
        super(cont, android.R.layout.simple_list_item_2);
        context = cont;
    }

    public void setData(List<Translation> data) {
        clear();
        if (data != null) {
            for (Translation translation : data) {
                add(translation);
            }
        }
    }
    
    
    /**
     * Populate new items in the list.
     */
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        
        if (convertView == null) {
            view = new TextView(context);
        } else {
            view = convertView;
        }

        Translation item = getItem(position);
        StringBuilder strBuilder = new StringBuilder();
        int i =0;
        for(String str:item.getJapaneseReb()){
        	i++;
        	strBuilder.append(str);
        	if(i < item.getJapaneseReb().size()){
        		strBuilder.append(", ");
        	}
        }
        i = 0;
        strBuilder.append("\n");
        List<List<String>> senses = item.getEnglishSense();
        if(senses != null){
	        for(String str:senses.get(0)){
	        	i++;
	        	strBuilder.append(str);
	        	if(i < senses.get(0).size()){
	        		strBuilder.append(", ");
	        	}
	        }
        }
        //Log.w("TranslationAdapter", strBuilder.toString());
        ((TextView)view).setText(strBuilder.toString());

        return view;
    } 
    
}
