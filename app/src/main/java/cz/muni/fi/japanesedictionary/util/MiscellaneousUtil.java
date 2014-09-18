package cz.muni.fi.japanesedictionary.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;

import java.util.List;

/**
 * Created by JonaSevcik on 15.9.13.
 */
public class MiscellaneousUtil {

    public static SpannableStringBuilder processKunyomi(Context context, List<String> kunyomis) {
        final int count = kunyomis.size();
        int i = 1;
        String lastBase = null;
        String lastKunyomi = null;
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for(String kunyomi: kunyomis){
            kunyomi = kunyomi.replaceAll("-", "～");
            final String[] basePlusOkurigana = kunyomi.split("\\.");
            if (basePlusOkurigana.length == 2) {
                final String nowKunyomi = basePlusOkurigana[0].concat(basePlusOkurigana[1]);
                if (kunyomi != null && lastBase != null && lastKunyomi != null && !(lastBase.equals(basePlusOkurigana[0]) || kunyomi.equals(nowKunyomi) || lastKunyomi.startsWith(nowKunyomi))) {
                    sb.append(System.getProperty("line.separator"));
                }
                sb.append(basePlusOkurigana[0]);
                int start = sb.length();
                sb.append(basePlusOkurigana[1]);
                final ForegroundColorSpan fcs = new ForegroundColorSpan(context.getResources().getColor(android.R.color.secondary_text_light_nodisable));
                sb.setSpan(fcs, start, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                lastBase = basePlusOkurigana[0];
                lastKunyomi = nowKunyomi;
            } else {
                final String nowKunyomi = kunyomi.replaceAll("～", "");
                if (i > 1 && lastBase != null && lastKunyomi != null && (!nowKunyomi.equals(lastKunyomi) && !nowKunyomi.startsWith(lastBase))) {
                    sb.append(System.getProperty("line.separator"));
                }
                sb.append(kunyomi);
                lastBase = nowKunyomi;
                lastKunyomi = nowKunyomi;
            }
            if(i < count){
                sb.append(", ");
            }
            i++;
        }
        return sb;
    }
}
