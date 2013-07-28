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

package cz.muni.fi.japanesedictionary.engine;

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

import java.util.List;

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
    private boolean mEnglish;
    private boolean mFrench;
    private boolean mDutch;
    private boolean mGerman;
    private LayoutInflater mInflater;
    private ListItemComparator mListComaparator;

    
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
        mListComaparator = new ListItemComparator();
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
    		setNotifyOnChange(false);
    		add(translation);
    		sort(mListComaparator);
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
            holder.japanese = (TextView)convertView.findViewById(R.id.japanese_item);
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
        	strBuilder.append(item.getJapaneseKeb().get(0)).append("  ");
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
        
        if(mEnglish && item.getEnglishSense() != null && item.getEnglishSense().size() > 0 ){
            holder.translation.setText(item.getEnglishSense().get(0).get(0));
        }else if(mGerman && item.getGermanSense() != null && item.getGermanSense().size() > 0 ){
            holder.translation.setText(item.getGermanSense().get(0).get(0));   	
        }else if(mFrench && item.getFrenchSense() != null && item.getFrenchSense().size() > 0){
            holder.translation.setText(item.getFrenchSense().get(0).get(0));      	
        }else if(mDutch && item.getDutchSense() != null && item.getDutchSense().size() > 0){
            holder.translation.setText(item.getDutchSense().get(0).get(0));    	
        }else{
            if(item.getEnglishSense() != null && item.getEnglishSense().size() > 0){
            	holder.translation.setText(item.getEnglishSense().get(0).get(0));
            	
            }        	
        }

        return convertView;
    }  
    
}
