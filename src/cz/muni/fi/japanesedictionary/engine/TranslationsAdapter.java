package cz.muni.fi.japanesedictionary.engine;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
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
	    TextView japanese;
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
            holder.japanese = (TextView)convertView.findViewById(R.id.japanese);
            holder.translation = (TextView)convertView.findViewById(R.id.translation);
            
            convertView.setTag(holder);
        } else {
        	holder = (TranslationsViewHolder) convertView.getTag();
        }
        Translation item = getItem(position);
        StringBuilder strBuilder = new StringBuilder();
        boolean write = false;
        int writeLength = 0;
        if(item.getJapaneseKeb() != null && item.getJapaneseKeb().size() > 0){
        	strBuilder.append(item.getJapaneseKeb().get(0)+"  ");
        	writeLength = item.getJapaneseKeb().get(0).length();
        	write = true;
        }
        if(item.getJapaneseReb() != null && item.getJapaneseReb().size() > 0){
        	strBuilder.append(item.getJapaneseReb().get(0));   	
        }
        
        if(write){
	        SpannableStringBuilder sb = new SpannableStringBuilder(strBuilder);
	        ForegroundColorSpan color = new ForegroundColorSpan(Color.WHITE); 
	        TextAppearanceSpan apearence = new TextAppearanceSpan(mContext, android.R.style.TextAppearance_Medium); 
	        sb.setSpan(apearence, 0, writeLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
	        sb.setSpan(color, 0, writeLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
	        holder.japanese.setText(sb);
        }else{
        	holder.japanese.setText(strBuilder);
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
