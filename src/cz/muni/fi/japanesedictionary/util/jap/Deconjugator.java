package cz.muni.fi.japanesedictionary.util.jap;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import cz.muni.fi.japanesedictionary.entity.Predicate;

/**
 *
 */
public class Deconjugator {

    public static Set<Predicate> deconjugate(String predicate) {
        Set<Predicate> predicates = new LinkedHashSet<Predicate>();
        TreeSet<String> predicateStrings = new TreeSet<String>();
        predicateStrings.add(predicate);

        while (!predicateStrings.isEmpty()) {
            String first = predicateStrings.first();
            char[] charArr = first.toCharArray();
            int length = charArr.length;
            int predicatesSize = predicates.size();

            if (length > 1) {
                switch (charArr[length - 1]) {
                    case 'た':
                        processTeTa(predicates, PredicateFormEnum.PAST, charArr, length, predicate);
                        break;
                    case 'だ':
                        processDeDa(predicates, PredicateFormEnum.PAST, charArr, length, predicate);
                        break;
                    case 'て':
                        processTeTa(predicates, PredicateFormEnum.TE, charArr, length, predicate);
                        break;
                    case 'で':
                        processDeDa(predicates, PredicateFormEnum.TE, charArr, length, predicate);
                        break;
                    case 'い':
                        processNegative(predicates, charArr, length, predicate);
                        break;
                    case 'る':
                        processRu(predicates, charArr, length, predicate);
                        break;
                    case 'す':
                        processSu(predicates, charArr, length, predicate);
                }
            }
            if (predicatesSize != predicates.size()) {
                for (Predicate p: predicates) {
                    if (p.getForm() != PredicateFormEnum.CAUSATIVE) { //to avoid causative to be followed by potential form
                        predicateStrings.add(p.getPredicate());
                    }
                }
            }
            predicateStrings.remove(first);
        }

        return predicates;
    }

    private static void processSu(Set<Predicate> predicates, char[] charArr, int length, String predicate) {
        if (length > 2) {
            switch (charArr[length - 2]) {
                case 'さ':
                    if (charArr[length - 3] == 'こ' || charArr[length - 3] == '来') {
                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("くる")));
                    } else {
                        Predicate p = new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("する"));
                        p.setSuru(true);
                        predicates.add(p);
                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("る")));
                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("す")));
                    }
                    break;
                case 'か':
                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("く")));
                    break;
                case 'が':
                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("ぐ")));
                    break;
                case 'ば':
                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("ぶ")));
                    break;
                case 'た':
                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("つ")));
                    break;
                case 'ま':
                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("む")));
                    break;
                case 'ら':
                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("る")));
                    break;
                case 'な':
                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("ぬ")));
                    break;
                case 'わ':
                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat("う")));
                    break;
            }
        }
    }

    private static void processRu(Set<Predicate> predicates, char[] charArr, int length, String predicate) {
        if (length > 2) {
            switch (charArr[length - 2]) {
                case 'え':
                    predicates.add(new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 2).concat("う")));
                    break;
                case 'け':
                    predicates.add(new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 2).concat("く")));
                    break;
                case 'て':
                    predicates.add(new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 2).concat("つ")));
                    break;
                case 'ね':
                    predicates.add(new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 2).concat("ぬ")));
                    break;
                case 'べ':
                    predicates.add(new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 2).concat("ぶ")));
                    break;
                case 'げ':
                    predicates.add(new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 2).concat("ぐ")));
                    break;
                case 'め':
                    predicates.add(new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 2).concat("む")));
                    break;
                case 'せ':
                    switch (charArr[length - 3]) {
                        case 'さ':
                            if (length > 3) {
                                if (charArr[length - 4] == 'こ' || charArr[length - 4] == '来') {
                                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 4).concat("くる")));
                                } else {
                                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("る")));
                                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("す")));
                                }
                            } else {
                                Predicate p = new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("する"));
                                p.setSuru(true);
                                predicates.add(p);
                            }
                            break;
                        case '為':
                            if(length > 3) {
                                predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("す")));
                            } else {
                                Predicate p = new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("する"));
                                p.setSuru(true);
                                predicates.add(p);
                            }
                            break;
                        case 'か':
                            predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("く")));
                            break;
                        case 'が':
                            predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("ぐ")));
                            break;
                        case 'ば':
                            predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("ぶ")));
                            break;
                        case 'た':
                            predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("つ")));
                            break;
                        case 'ま':
                            predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("む")));
                            break;
                        case 'ら':
                            predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("る")));
                            break;
                        case 'な':
                            predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("ぬ")));
                            break;
                        case 'わ':
                            predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("う")));
                            break;
                        default:
                            predicates.add(new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length-2).concat("す")));
                    }
                    break;
                case 'れ':
                    switch (charArr[length - 3]) {
                        case 'ら':
                            if (length > 3 && (charArr[length - 4] == 'こ' || charArr[length - 4] == '来')) {
                                predicates.add(new Predicate(PredicateFormEnum.POTENTIAL_PASSIVE, predicate.substring(0, length - 4).concat("くる")));
                            } else
                                predicates.add(new Predicate(PredicateFormEnum.POTENTIAL_PASSIVE, predicate.substring(0, length - 3).concat("る")));
                            break;
                        case 'さ':
                            if (length > 3) {
                                switch (charArr[length - 4]) {
                                    case 'か':
                                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVEPASSIVE, predicate.substring(0, length - 4).concat("く")));
                                        break;
                                    case 'が':
                                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVEPASSIVE, predicate.substring(0, length - 4).concat("ぐ")));
                                        break;
                                    case 'ば':
                                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVEPASSIVE, predicate.substring(0, length - 4).concat("ぶ")));
                                        break;
                                    case 'た':
                                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVEPASSIVE, predicate.substring(0, length - 4).concat("つ")));
                                        break;
                                    case 'ま':
                                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVEPASSIVE, predicate.substring(0, length - 4).concat("む")));
                                        break;
                                    case 'ら':
                                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVEPASSIVE, predicate.substring(0, length - 4).concat("る")));
                                        break;
                                    case 'な':
                                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVEPASSIVE, predicate.substring(0, length - 4).concat("ぬ")));
                                        break;
                                    case 'わ':
                                        predicates.add(new Predicate(PredicateFormEnum.CAUSATIVEPASSIVE, predicate.substring(0, length - 4).concat("う")));
                                        break;
                                    default:
                                        predicates.add(new Predicate(PredicateFormEnum.PASSIVE, predicate.substring(0, length - 3).concat("す")));
                                        Predicate p = new Predicate(PredicateFormEnum.PASSIVE, predicate.substring(0, length - 3).concat("する"));
                                        p.setSuru(true);
                                        predicates.add(p);
                                }
                            }
                            break;
                        case '為':
                            Predicate p = new Predicate(PredicateFormEnum.PASSIVE, predicate.substring(0, length - 3).concat("する"));
                            p.setSuru(true);
                            predicates.add(p);
                            break;
                        default:
                            predicates.add(new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 2).concat("る")));
                    }
            }
        }
    }

    private static void processTeTa(Set<Predicate> predicates, PredicateFormEnum form, char[] charArr, int length, String predicate) {
        if (length == 2) {
            switch (charArr[length - 2]) {
                case 'き':
                    predicates.add(new Predicate(form, "くる"));
                    break;
                case '来':
                    predicates.add(new Predicate(form, "来る"));
                    break;
                case 'し':
                    Predicate p = new Predicate(form, "する");
                    p.setSuru(true);
                    predicates.add(p);
                    break;
            }
        } else {
            if (charArr[length - 2] == 'い') {
                predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("く")));
            }
            if (charArr[length - 2] == 'し') {
                predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("す")));
            }
            if (charArr[length - 2] == 'っ') {
                if (charArr[length - 3] == 'か') {
                    predicates.add(new Predicate(form, predicate.substring(0, length - 3).concat("い")));
                } else if (length == 3 && (charArr[length - 3] == 'い') || (charArr[length - 3] == '行')) {
                    predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("く")));
                } else {
                    predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("う")));
                    predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("る")));
                }
            }
        }
    }

    private static void processDeDa(Set<Predicate> predicates, PredicateFormEnum form, char[] charArr, int length, String predicate) {
        if (length > 2) {
            if (charArr[length - 2] == 'い') {
                predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("ぐ")));
            }
            if (charArr[length - 2] == 'ん') {
                predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("む")));
                predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("ぶ")));
                predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("ぬ")));
            }
        }
    }

    private static void processNegative(Set<Predicate> predicates, char[] charArr, int length, String predicate) {
        if (charArr[length - 2] == 'な') {
            if (length == 2) {
                predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, "ある"));
            } else {
                switch (charArr[length - 3]) {
                    case 'く':
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("い")));
                        break;
                    case 'わ':
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("う")));
                        break;
                    case 'か':
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("く")));
                        break;
                    case 'さ':
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("す")));
                        break;
                    case 'た':
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("つ")));
                        break;
                    case 'ま':
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("む")));
                        break;
                    case 'ら':
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("る")));
                        break;
                    case 'ば':
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("ぶ")));
                        break;
                    case 'が':
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("ぐ")));
                        break;
                    case 'し':
                    case '為':
                        Predicate p = new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("する"));
                        p.setSuru(true);
                        predicates.add(p);
                        break;
                    case 'こ':
                        if (length == 3) {
                            predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, "くる"));
                            break;
                        }
                    case '来':
                        if (length == 3) {
                            predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, "来る"));
                            break;
                        }
                    default:
                        predicates.add(new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 2).concat("る")));
                }

            }
        }
    }
}
