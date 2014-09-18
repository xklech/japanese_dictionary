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

import java.util.Comparator;

import cz.muni.fi.japanesedictionary.entity.Translation;


/**
 * Comparator used in TranslationAdapter. Compares two translation acording 
 * to the lentgh of their first japanese writing.
 * 
 * @author Jaroslav Klech
 */
public class ListItemComparator implements Comparator<Translation> {

	
	/**
	 * Compares two translations for lentgh of displayed japanese words for writing
	 * 
	 * @return -1 if the first translation has shorter writing, 0 if both writings have the same length and
	 * 1 when first translation has longer writing
	 */
	@Override
	public int compare(Translation lhs, Translation rhs) {
		int lengthKeb1 = (lhs != null && lhs.getJapaneseKeb() != null && lhs.getJapaneseKeb().get(0) != null)?lhs.getJapaneseKeb().get(0) .length():0;
		int lengthKeb2 = (rhs != null && rhs.getJapaneseKeb() != null && rhs.getJapaneseKeb().get(0) != null)?rhs.getJapaneseKeb().get(0) .length():0;
        if(lengthKeb1 == 0 || lengthKeb2 == 0){
            lengthKeb1 = (lhs != null && lhs.getJapaneseReb() != null && lhs.getJapaneseReb().get(0) != null)?lhs.getJapaneseReb().get(0) .length():0;
            lengthKeb2 = (rhs != null && rhs.getJapaneseReb() != null && rhs.getJapaneseReb().get(0) != null)?rhs.getJapaneseReb().get(0) .length():0;
        }
        int dif = lengthKeb1-lengthKeb2;
        if(dif == 0){
            if(lhs.isPrioritized() && !rhs.isPrioritized()){
                dif = -1;
            }else if (!lhs.isPrioritized() && rhs.isPrioritized()){
                dif = 1;
            }
        }
		return Integer.signum(dif);
	}


}
