package cz.muni.fi.japanesedictionary.util.jap;


/**
 * @author Jonáš Ševèík
 *
 */
public class TranscriptionConverter {

	public static String kunreiToHepburn(String word) {
		StringBuilder sb = new StringBuilder();
		char[] charArr = word.toLowerCase().toCharArray();
		boolean skipNext = false;
		
		for(int i = 0; i < charArr.length; i++) {
			if (skipNext) {
				skipNext = false;
				continue;
			}
			if (i + 1 == charArr.length) {
				sb.append(charArr[i]);
			} else {
				if(charArr[i] == 's' && (charArr[i+1] == 'i' || (skipNext = charArr[i+1] == 'y'))) {
					sb.append("sh");
				} else if (charArr[i] == 'z' && (charArr[i+1] == 'i' || (skipNext = charArr[i+1] == 'y'))) {
					sb.append("j");
				} else if(charArr[i] == 't') {
					if(charArr[i+1] == 'i' || (skipNext = charArr[i+1] == 'y')) {
						sb.append("ch");
					} else if (charArr[i+1] == 'u') {
						sb.append("ts");
					} else {
						sb.append(charArr[i]);
					}
			} else if (charArr[i] == 'h' && charArr[i + 1] == 'u'
						&& (i == 0 || (i > 0 && charArr[i - 1] != 's') && charArr[i - 1] != 'c')) {
		        	sb.append("f");
		        } else if (charArr[i] == 'd') { //exceptions
		        	if(charArr[i+1] == 'i' || (skipNext = charArr[i+1] == 'y')) {
						sb.append("xj");
					} else if (charArr[i+1] == 'u') {
						sb.append("xz");
					} else {
						sb.append(charArr[i]);
					}
		        } else {
		        	sb.append(charArr[i]);
		        }
			}
	    }
		
	    return sb.toString();
	}
}

