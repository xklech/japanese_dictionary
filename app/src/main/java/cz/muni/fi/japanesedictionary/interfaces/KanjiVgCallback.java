package cz.muni.fi.japanesedictionary.interfaces;

import com.caverock.androidsvg.SVG;

import java.util.List;

/**
 * KanjiVgCallback
 */
public interface KanjiVgCallback {
    public void kanjiVgLoaded(List<SVG> svgs);
}
