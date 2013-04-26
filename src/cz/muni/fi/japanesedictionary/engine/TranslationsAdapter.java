package cz.muni.fi.japanesedictionary.engine;

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

/**
 * Adapter for ResultFragmentList.
 * 
 * @author Jaroslav Klech 
 *
 */
public class TranslationsAdapter extends ArrayAdapter<Translation>{
    
	
	
	/**
	 * Static Translation holder for item View.
	 * @author Jaroslav Klech
	 *
	 */
	static class TranslationsViewHolder {
	    TextView read;
	    TextView write;
	    TextView translation;
	}
	
	
    private Context mContext;
    boolean mEnglish;
    boolean mFrench;
    boolean mDutch;
    boolean mGerman;
    LayoutInflater mInflater;
    
    /**
     * Constructor for TranslationsAdapter. Sets languages.
     * @param cont Enviroment context
     */
    public TranslationsAdapter(Context cont) {
        super(cont, R.layout.list_item);
        mContext = cont;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEnglish = sharedPrefs.getBoolean("language_english", false);
        mFrench = sharedPrefs.getBoolean("language_french", false);        
        mDutch = sharedPrefs.getBoolean("language_dutch", false);
        mGerman = sharedPrefs.getBoolean("language_german", false);
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Sets data tu adapter. And notifies change of list.
     * 
     * @param data list to be set to adapter
     */
    public void setData(List<Translation> data) {
        clear();
        if (data != null) {
            for (Translation translation : data) {
                add(translation);
            } 
        }
        notifyDataSetChanged();
    }
    
    /**
     * Adds individual Translation to adapter
     * 
     * @param translation to be added if not null
     */
    public void addListItem(Translation translation){
    	if(translation != null){
    		add(translation);
    		notifyDataSetChanged();
    	}
    }
    
    /**
     * Constructs view for item in list.
     */
    @Override 
    public View getView(int position, View convertView, ViewGroup parent) {
    	TranslationsViewHolder holder;
        if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.list_item, parent,false);    
            holder = new TranslationsViewHolder();
            holder.read = (TextView)convertView.findViewById(R.id.jap_read);
            holder.write = (TextView)convertView.findViewById(R.id.jap_write);
            holder.translation = (TextView)convertView.findViewById(R.id.translation);
            
            convertView.setTag(holder);
        } else {
        	holder = (TranslationsViewHolder) convertView.getTag();
        }
        Translation item = getItem(position);
        if(item.getJapaneseReb() != null && item.getJapaneseReb().size() > 0){
        	holder.read.setText(item.getJapaneseReb().get(0));
        }
        if(item.getJapaneseKeb() != null && item.getJapaneseKeb().size() > 0){
        	holder.write.setText(item.getJapaneseKeb().get(0));
        	holder.write.setVisibility(View.VISIBLE);
        }else{
        	holder.write.setVisibility(View.GONE);
        }
        if(mEnglish){
            if(item.getEnglishSense() != null){
            	holder.translation.setText(item.getEnglishSense().get(0).get(0));
            } 
        }else if(mGerman){
            if(item.getGermanSense() != null){
            	holder.translation.setText(item.getGermanSense().get(0).get(0));
            }       	
        }else if(mFrench){
            if(item.getFrenchSense() != null){
            	holder.translation.setText(item.getFrenchSense().get(0).get(0));
            }       	
        }else if(mDutch){
            if(item.getDutchSense() != null){
            	holder.translation.setText(item.getDutchSense().get(0).get(0));
            }       	
        }else{
            if(item.getEnglishSense() != null){
            	holder.translation.setText(item.getEnglishSense().get(0).get(0));
            }        	
        }

        return convertView;
    }  
    
}
