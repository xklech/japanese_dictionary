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
    public static int convertDip2Pixels(Context context, int dip) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    public static SpannableStringBuilder processKunyomi(Context context, List<String> kunyomis) {
        final int count = kunyomis.size();
        int i =1;
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for(String kunyomi: kunyomis){
            kunyomi = kunyomi.replaceAll("-", "～");
            final String[] basePlusOkurigana = kunyomi.split("\\.");
            if (basePlusOkurigana.length == 2) {
                sb.append(basePlusOkurigana[0]);
                int start = sb.length();
                sb.append(basePlusOkurigana[1]);
                final ForegroundColorSpan fcs = new ForegroundColorSpan(context.getResources().getColor(android.R.color.secondary_text_light_nodisable));
                sb.setSpan(fcs, start, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                sb.append(kunyomi);
            }
            if(i < count){
                sb.append(", ");
            }
            i++;
        }
        return sb;
    }
}
