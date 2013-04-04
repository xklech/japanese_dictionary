package cz.muni.fi.japanesedictionary.main;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cz.muni.fi.japanesedictionary.R;
import cz.muni.fi.japanesedictionary.entity.Translation;

public class TranslationsAdapter extends ArrayAdapter<Translation>{
    
    private Context context;
    boolean english;
    boolean french;
    boolean dutch;
    boolean german;
    LayoutInflater inflater;
    
    public TranslationsAdapter(Context cont) {
        super(cont, android.R.layout.simple_list_item_2);
        context = cont;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        english = sharedPrefs.getBoolean("language_english", false);
        french = sharedPrefs.getBoolean("language_french", false);        
        dutch = sharedPrefs.getBoolean("language_dutch", false);
        german = sharedPrefs.getBoolean("language_german", false);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        TextView write;
        TextView read;
        TextView translate;
        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item, null);         
        } else {
            view = convertView;
        }
        write = (TextView)view.findViewById(R.id.jap_write);
        read = (TextView)view.findViewById(R.id.jap_read);
        translate = (TextView)view.findViewById(R.id.translation);
        Translation item = getItem(position);
        if(item.getJapaneseReb() != null){
        	read.setText(item.getJapaneseReb().get(0));
        }
        if(item.getJapaneseKeb() != null){
        	write.setText(item.getJapaneseKeb().get(0));
        	write.setVisibility(View.VISIBLE);
        }else{
        	write.setVisibility(View.GONE);
        }
        if(english){
            if(item.getEnglishSense() != null){
            	translate.setText(item.getEnglishSense().get(0).get(0));
            } 
        }else if(german){
            if(item.getGermanSense() != null){
            	translate.setText(item.getGermanSense().get(0).get(0));
            }       	
        }else if(french){
            if(item.getFrenchSense() != null){
            	translate.setText(item.getFrenchSense().get(0).get(0));
            }       	
        }else if(dutch){
            if(item.getDutchSense() != null){
            	translate.setText(item.getDutchSense().get(0).get(0));
            }       	
        }else{
            if(item.getEnglishSense() != null){
            	translate.setText(item.getEnglishSense().get(0).get(0));
            }        	
        }

        
        
        return view;
    }  
    
}
