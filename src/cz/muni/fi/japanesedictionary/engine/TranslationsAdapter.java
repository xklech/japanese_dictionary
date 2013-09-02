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

import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
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
    private boolean mEnglish;
    private boolean mFrench;
    private boolean mDutch;
    private boolean mGerman;
    private LayoutInflater mInflater;
    private ListItemComparator mListComaparator;
    private String mLastSearchedKeb;
    private boolean mIsExact;


    /**
     * Constructor for TranslationsAdapter. Sets languages.
     * @param cont Enviroment context
     */
    public TranslationsAdapter(Context cont) {
        super(cont, R.layout.list_item);
        mContext = cont;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEnglish = sharedPrefs.getBoolean("language_english", false);
        Log.e("adapter", "english: "+mEnglish);
        mFrench = sharedPrefs.getBoolean("language_french", false);
        mDutch = sharedPrefs.getBoolean("language_dutch", false);
        mGerman = sharedPrefs.getBoolean("language_german", false);
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListComaparator = new ListItemComparator();
        mIsExact = false;
    }

    /**
     * Sets data to adapter. And notifies change of list.
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
        boolean isDeconjugated = true;
        if(item.getJapaneseKeb() != null && item.getJapaneseKeb().size() > 0){
            strBuilder.append(item.getJapaneseKeb().get(0)).append("  ");
            writeLength = item.getJapaneseKeb().get(0).length();
            write = true;
        }
        if(item.getJapaneseReb() != null && item.getJapaneseReb().size() > 0){
            for (int i = 0; i < item.getJapaneseReb().size(); i++) {
                strBuilder.append(item.getJapaneseReb().get(i));
                if (i < item.getJapaneseReb().size() - 1) {
                    strBuilder.append(", ");
                }
                //was it deconjugated? -> color
                if (isDeconjugated && item.getJapaneseReb().get(i).equals(mLastSearchedKeb)) {
                    isDeconjugated = false;
                }
            }
        }

        if(write){
            SpannableStringBuilder sb = new SpannableStringBuilder(strBuilder);
            boolean alternative = false;
            if(mLastSearchedKeb != null && !item.getJapaneseKeb().get(0).contains(mLastSearchedKeb)){
                //search alternatives
                for(String keb:item.getJapaneseKeb()){
                    if(keb.contains(mLastSearchedKeb)){
                        alternative = true;
                        break;
                    }
                }
            }
            ForegroundColorSpan color;
            if(alternative){
                color= new ForegroundColorSpan(Color.GREEN);
            }else if (isDeconjugated && mIsExact && mLastSearchedKeb != null) {
                color= new ForegroundColorSpan(Color.YELLOW);
            } else {
                color= new ForegroundColorSpan(Color.WHITE);
            }

            TextAppearanceSpan appearance = new TextAppearanceSpan(mContext, android.R.style.TextAppearance_Medium);
            sb.setSpan(appearance, 0, writeLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(color, 0, writeLength, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.japanese.setText(sb);
        }else{
            holder.japanese.setText(strBuilder);
        }

        if(mEnglish && item.getEnglishSense() != null && item.getEnglishSense().size() > 0 ){
            holder.translation.setText(formatSenses(item.getEnglishSense()));
        }else if(mFrench && item.getFrenchSense() != null && item.getFrenchSense().size() > 0){
            holder.translation.setText(formatSenses(item.getFrenchSense()));
        }else if(mDutch && item.getDutchSense() != null && item.getDutchSense().size() > 0){
            holder.translation.setText(formatSenses(item.getDutchSense()));
        }else if(mGerman && item.getGermanSense() != null && item.getGermanSense().size() > 0 ){
            holder.translation.setText(formatSenses(item.getGermanSense()));
        }else{
            if(item.getEnglishSense() != null && item.getEnglishSense().size() > 0){
                holder.translation.setText(item.getEnglishSense().get(0).get(0));
            }else{
                holder.translation.setText("");
            }
        }

        return convertView;
    }

    /**
     *
     * @param senses senses to append
     * @return formatted and appended String of senses
     */
    private String formatSenses(List<List<String>> senses) {
        StringBuilder sb = new StringBuilder();
        for (int i= 0; i < senses.size(); i++) {
            sb.append(senses.get(i).get(0));
            if (i < senses.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Updates language preferences
     *
     * @return true if some language was changed
     * 	       false if there wasn't change
     */
    private boolean updateLanguages(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
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
        return changed;
    }

    public void updateAdapter(){
        if(updateLanguages()){
            notifyDataSetChanged();
        }
    }



    public void setLastSearchedKeb(String lastSearchedKeb) {
        if(lastSearchedKeb == null || Pattern.matches("\\p{Latin}*", lastSearchedKeb)){
            this.mLastSearchedKeb = null;
            return ;
        }
        this.mLastSearchedKeb = lastSearchedKeb;

    }

    public void setIsExact(boolean isExact) {
        mIsExact = isExact;
    }
}