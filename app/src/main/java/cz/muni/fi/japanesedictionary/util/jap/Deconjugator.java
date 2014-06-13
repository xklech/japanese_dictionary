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
        Set<Predicate> predicates = new LinkedHashSet<>();
        TreeSet<String> predicateStrings = new TreeSet<>();
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
                        Predicate p = new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("くる"));
                        p.setKuru(true);
                        predicates.add(p);
                    } else {
                        Predicate p = new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 2).concat(length == 2? "する": ""));
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
                case '来':
                    if (charArr[length - 3] == '出') {
                        Predicate p = new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 3).concat(length == 3? "する": ""));
                        p.setSuru(true);
                        predicates.add(p);
                    }
                    break;
                case 'き':
                    if (charArr[length - 3] == 'で') {
                        Predicate p = new Predicate(PredicateFormEnum.POTENTIAL, predicate.substring(0, length - 3).concat(length == 3? "する": ""));
                        p.setSuru(true);
                        predicates.add(p);
                    }
                    break;
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
                                    Predicate p = new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 4).concat("くる"));
                                    p.setKuru(true);
                                    predicates.add(p);
                                } else {
                                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("る")));
                                    predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("す")));
                                }
                            } else {
                                Predicate p = new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat(length == 3? "する": ""));
                                p.setSuru(true);
                                predicates.add(p);
                            }
                            break;
                        case '為':
                            if(length > 3) {
                                predicates.add(new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat("す")));
                            } else {
                                Predicate p = new Predicate(PredicateFormEnum.CAUSATIVE, predicate.substring(0, length - 3).concat(length == 3? "する": ""));
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
                                Predicate p = new Predicate(PredicateFormEnum.POTENTIAL_PASSIVE, predicate.substring(0, length - 4).concat("くる"));
                                p.setKuru(true);
                                predicates.add(p);
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
                                        Predicate p = new Predicate(PredicateFormEnum.PASSIVE, predicate.substring(0, length - 3).concat(length == 3? "する": ""));
                                        p.setSuru(true);
                                        predicates.add(p);
                                }
                            }
                            break;
                        case '為':
                            Predicate p = new Predicate(PredicateFormEnum.PASSIVE, predicate.substring(0, length - 3).concat(length == 3? "する": ""));
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
                case '来':
                    Predicate p = new Predicate(form, "くる");
                    p.setKuru(true);
                    predicates.add(p);
                    break;
                case 'し':
                case '為':
                    Predicate pr = new Predicate(form, length == 2? "する": "");
                    pr.setSuru(true);
                    predicates.add(pr);
                    break;
            }
        } else {
            if (charArr[length - 2] == 'い') {
                predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("く")));
            }
            if (charArr[length - 2] == 'し') {
                predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("す")));
            }
            if (charArr[length - 2] == 'く' && form == PredicateFormEnum.TE) {
                Predicate p = new Predicate(PredicateFormEnum.TE, predicate.substring(0, length - 2).concat("い"));
                p.setIAdjective(true);
                predicates.add(p);
            }
            if (charArr[length - 2] == 'っ') {
                if (charArr[length - 3] == 'か' && form == PredicateFormEnum.PAST) {
                    Predicate p = new Predicate(PredicateFormEnum.PAST, predicate.substring(0, length - 3).concat("い"));
                    p.setIAdjective(true);
                    predicates.add(p);
                } else if (charArr[length - 3] == '行') {
                    Predicate p = new Predicate(form, predicate.substring(0, length - 2).concat("く"));
                    p.setIku(true);
                    predicates.add(p);
                } else {
                    if (charArr[length - 3] == 'い'){
                        Predicate p = new Predicate(form, predicate.substring(0, length - 2).concat("く"));
                        p.setIku(true);
                        predicates.add(p);
                    }
                    predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("る")));
                    predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("う")));
                    predicates.add(new Predicate(form, predicate.substring(0, length - 2).concat("つ")));
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
                        Predicate p = new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat("い"));
                        p.setIAdjective(true);
                        predicates.add(p);
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
                        Predicate pre = new Predicate(PredicateFormEnum.NEGATIVE, predicate.substring(0, length - 3).concat(length == 3? "する": ""));
                        pre.setSuru(true);
                        predicates.add(pre);
                        break;
                    case 'こ':
                        if (length == 3) {
                            Predicate pr = new Predicate(PredicateFormEnum.NEGATIVE, "くる");
                            pr.setKuru(true);
                            predicates.add(pr);
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

    public static void main(String[] args) {
        System.out.println(deconjugate("きいた"));
        System.out.println(deconjugate("でした"));
        System.out.println(deconjugate("よかった"));
        System.out.println(deconjugate("よった"));
        System.out.println(deconjugate("行った"));
        System.out.println(deconjugate("まいった"));
        System.out.println(deconjugate("いって"));
        System.out.println(deconjugate("泳いで"));
        System.out.println(deconjugate("死んだ"));
        System.out.println(deconjugate("行かない"));
        System.out.println(deconjugate("寒くなかった"));
        System.out.println(deconjugate("食べない"));
        System.out.println(deconjugate("しない"));
        System.out.println(deconjugate("こない"));
        System.out.println(deconjugate("来ない"));
        System.out.println(deconjugate("できない"));
        System.out.println(deconjugate("出来ない"));
        System.out.println(deconjugate("燃える"));//!!!
        System.out.println(deconjugate("燃えない"));//!!!
        System.out.println(deconjugate("会える"));
        System.out.println(deconjugate("行ける"));
        System.out.println(deconjugate("売れる"));
        System.out.println(deconjugate("指せる"));//!!!
        System.out.println(deconjugate("させる"));
        System.out.println(deconjugate("こられる"));
        System.out.println(deconjugate("来られる"));
        System.out.println(deconjugate("待たせる"));
        System.out.println(deconjugate("こさせる"));
        System.out.println(deconjugate("こさせなかった"));
        System.out.println(deconjugate("こさせられなかった"));
        System.out.println(deconjugate("開けなかった"));
        System.out.println(deconjugate("ありえない"));
        System.out.println(deconjugate("した"));
        System.out.println(deconjugate("ない"));
        System.out.println(deconjugate("やった"));
        System.out.println(deconjugate("食べさせられる"));
        System.out.println(deconjugate("行かせられる"));
        System.out.println(deconjugate("行かされる"));
        System.out.println(deconjugate("たべる")); //!!
        System.out.println(deconjugate("話される")); //!!
    }
}
