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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;

import org.apache.lucene.index.IndexReader;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.standard.StandardQueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.muni.fi.japanesedictionary.database.GlossaryReaderContract;
import cz.muni.fi.japanesedictionary.entity.Predicate;
import cz.muni.fi.japanesedictionary.entity.Translation;
import cz.muni.fi.japanesedictionary.interfaces.SearchListener;
import cz.muni.fi.japanesedictionary.parser.ParserService;
import cz.muni.fi.japanesedictionary.util.jap.Deconjugator;
import cz.muni.fi.japanesedictionary.util.jap.RomanizationEnum;
import cz.muni.fi.japanesedictionary.util.jap.TranscriptionConverter;

/**
 * Loader for ResultFragmentList. Searches JMdict for match with expression.
 * 
 * @author Jaroslav Klech
 * 
 */
public class FragmentListAsyncTask extends
		AsyncTask<String, Translation, List<Translation>> {

	private Context mContext;
	final private SearchListener mSearchListener;
    private static final String LOG_TAG = "FragmentListAsyncTask";

	/**
	 * Constructor for FragmentListAsyncTask
	 * 
	 * @param list
	 *            List implementing SearchListener
	 * @param context
	 *            environment context
	 */
	public FragmentListAsyncTask(SearchListener list, Context context) {
		mSearchListener = list;
		mContext = context;
	}

	/**
	 * Loads translation using Lucene
	 */
	@Override
	protected List<Translation> doInBackground(String... params) {
		String expression = params[0];
		String part = params[1];

		SharedPreferences settings = mContext.getSharedPreferences(
				ParserService.DICTIONARY_PREFERENCES, 0);
		String pathToDictionary = settings.getString("pathToDictionary", null);
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		final boolean englishBool = sharedPrefs.getBoolean("language_english",
				false);
		final boolean frenchBool = sharedPrefs.getBoolean("language_french",
				false);
		final boolean dutchBool = sharedPrefs.getBoolean("language_dutch",
				false);
		final boolean germanBool = sharedPrefs.getBoolean("language_german",
				false);
        final boolean russianBool = sharedPrefs.getBoolean("language_russian",
                false);
        final boolean searchOnlyFavorised = sharedPrefs.getBoolean("search_only_favorite",
                false);
        final boolean searchDeinflected = sharedPrefs.getBoolean("search_deinflected", false);

		final List<Translation> translations = new ArrayList<>();

		if (expression == null) {
			// first run
			Log.i(LOG_TAG, "First run - last 10 translations ");
			GlossaryReaderContract database = new GlossaryReaderContract(
					mContext);
			List<Translation> translationsTemp = database
					.getLastTranslations(10);
			database.close();
			return translationsTemp;
		}
		
		if (pathToDictionary == null) {
			Log.e(LOG_TAG, "No path to jmdict dictionary");
			return null;
		}
		File file = new File(pathToDictionary);
		if (!file.exists() || !file.canRead()) {
			Log.e(LOG_TAG,
					"Can't read jmdict dictionary directory");
			return null;
		}

		if (expression.length() < 1) {
			Log.w(LOG_TAG, "No expression to translate");
			return null;
		}
		Analyzer analyzer = new CJKAnalyzer(Version.LUCENE_36);

        IndexReader reader;
		try {
			final String search;
            final String hiragana;
			boolean onlyReb = false;

			if (Pattern.matches("\\p{Latin}*", expression)) {
				// only romaji
				onlyReb = true;
				Log.i(LOG_TAG,
						"Only latin letters, converting to hiragana. ");
				expression = TranscriptionConverter.kunreiToHepburn(expression);
				expression = RomanizationEnum.Hepburn.toHiragana(expression);
			}
            hiragana = expression;

            expression = insertSpaces(expression);

            switch(part){
                case "end":
                    search = "\"" + expression + "lucenematch\"";
                    break;
                case "beginning":
                    search = "\"lucenematch " + expression + "\"";
                    break;
                case "middle":
                    search = "\"" + expression + "\"";
                    break;
                default:
                    if (searchDeinflected) {
                        StringBuilder sb = new StringBuilder("\"lucenematch " + expression + "lucenematch\"");
                        for (Predicate predicate: Deconjugator.deconjugate(hiragana)) {
                            if (predicate.isSuru()) {
                                sb.append(" OR ").append("(\"lucenematch ").append(insertSpaces(predicate.getPredicate())).append("lucenematch\" AND (pos:vs OR pos:vs-c OR pos:vs-s OR pos:vs-i))");
                            } else if (predicate.isKuru()) {
                                sb.append(" OR ").append("(\"lucenematch ").append(insertSpaces(predicate.getPredicate())).append( "lucenematch\" AND pos:vk)");
                            } else if (predicate.isIku()) {
                                sb.append(" OR ").append("(\"lucenematch ").append(insertSpaces(predicate.getPredicate())).append("lucenematch\" AND pos:v5k-s)");
                            } else if (predicate.isIAdjective()) {
                                sb.append(" OR ").append("(\"lucenematch ").append(insertSpaces(predicate.getPredicate())).append("lucenematch\" AND pos:adj-i)");
                            } else
                                sb.append(" OR ").append("(\"lucenematch ").append(insertSpaces(predicate.getPredicate())).append("lucenematch\" AND (pos:v1 OR pos:v2 OR pos:v5 OR pos:vz OR pos:vi OR pos:vn OR pos:vr))");
                        }
                        search = sb.toString();
                    } else {
                        search = "\"lucenematch " + expression + "lucenematch\"";
                    }
            }
			Log.i(LOG_TAG, " Searching for: " + search);

			Query q;
			if (onlyReb) {
				q = (new QueryParser(Version.LUCENE_36, "index_japanese_reb",
						analyzer)).parse(search);
			} else {
				StandardQueryParser parser = new StandardQueryParser(analyzer);
				q = parser.parse(search, "japanese");
			}

			Directory dir = FSDirectory.open(file);
			reader =  IndexReader.open(dir);
            final IndexSearcher searcher = new IndexSearcher(reader);
			Collector collector = new Collector() {
				int max = 1000;
				int count = 0;
                private int docBase;

				@Override
				public boolean acceptsDocsOutOfOrder() {
					return true;
				}

				@Override
				public void collect(int docID) throws IOException {
					Document d = searcher.doc(docID + docBase);
					Translation translation = new Translation();
                    String prioritized = d.get("prioritized");
                    if(searchOnlyFavorised && prioritized == null){
                        return ;
                    }
                    if(prioritized != null){
                        //is prioritized
                        translation.setPrioritized(true);
                    }

                    String ruby = d.get("ruby");

                    if(ruby != null && ruby.length() > 0){
                        translation.setRuby(ruby);
                    }

					String japanese_keb = d.get("japanese_keb");
					if (japanese_keb != null && japanese_keb.length() != 0) {
						translation.parseJapaneseKeb(japanese_keb);
					}

					String japanese_reb = d.get("japanese_reb");
					if (japanese_reb != null && japanese_reb.length() != 0) {
						translation.parseJapaneseReb(japanese_reb);
					}

					String english = d.get("english");
					if (english != null && english.length() != 0) {
						translation.parseEnglish(english);
					}

					String french = d.get("french");
					if (french != null && french.length() != 0) {
						translation.parseFrench(french);
					}

					String dutch = d.get("dutch");
					if (dutch != null && dutch.length() != 0) {
						translation.parseDutch(dutch);
					}

					String german = d.get("german");
					if (german != null && german.length() != 0) {
						translation.parseGerman(german);
					}

                    String russian = d.get("russian");
                    if (russian != null && russian.length() != 0) {
                        translation.parseRussian(russian);
                    }


					if ((englishBool && translation.getEnglishSense() != null)
							|| (dutchBool && translation.getDutchSense() != null)
							|| (germanBool && translation.getGermanSense() != null)
							|| (frenchBool && translation.getFrenchSense() != null)
                            || (russianBool && translation.getRussianSense() != null)) {

						count++;
						if (count < max) {
							if (!FragmentListAsyncTask.this.isCancelled()) {
								FragmentListAsyncTask.this.publishProgress(translation);
								translations.add(translation);
							} else {
								translations.clear();
								throw new IOException("Loader canceled");
							}
						} else {
							throw new IOException("Max exceeded");
						}
					}
				}

                @Override
                public void setNextReader(IndexReader reader, int docBas) throws IOException {
                    docBase = docBas;
                }

				@Override
				public void setScorer(Scorer arg0) throws IOException {
				}

			};

			searcher.search(q, collector);
            reader.close();
		} catch (IOException ex) {
			Log.e(LOG_TAG, "IO Exception:  " + ex.toString());
			return translations;
		} catch (Exception ex) {
			Log.e(LOG_TAG, "Exception: " + ex.toString());
			return null;
		}

		return translations.isEmpty() ? null : translations;
	}

    private String insertSpaces(String expression) {
        StringBuilder searchBuilder = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            String character = String.valueOf(expression.charAt(i));
            searchBuilder.append(character).append(' ');
        }
        return searchBuilder.toString();
    }

    /**
	 * Adding individual translations to list fragment
	 */
	@Override
	protected void onProgressUpdate(Translation... values) {
		if (!isCancelled()) {
			for(Translation trans:values){
				mSearchListener.onResultFound(trans);
			}
		}
		super.onProgressUpdate(values);
	}

	/**
	 * Sets list of translations to list fragment
	 */
	@Override
	protected void onPostExecute(List<Translation> result) {
		if (!isCancelled()) {
			mSearchListener.onLoadFinished(result);
		}

		super.onPostExecute(result);
	}

}
