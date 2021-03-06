package cz.muni.fi.japanesedictionary.entity;

import cz.muni.fi.japanesedictionary.util.jap.PredicateFormEnum;

/**
 * Created by NICKT on 14.8.13.
 */
public class Predicate {
    private PredicateFormEnum form;
    private String predicate;
    private boolean isSuru;
    private boolean isKuru;
    private boolean isIku;
    private boolean isIAdjective;

    public Predicate (PredicateFormEnum form, String predicate) {
        this.form = form;
        this.predicate = predicate;
        isSuru = false;
        isKuru = false;
        isIku = false;
    }

    public boolean isSuru() {
        return isSuru;
    }

    public void setSuru(boolean suru) {
        isSuru = suru;
    }

    public boolean isKuru() {
        return isKuru;
    }

    public void setKuru(boolean kuru) {
        isKuru = kuru;
    }

    public boolean isIku() {
        return isIku;
    }

    public void setIku(boolean iku) {
        isIku = iku;
    }

    public boolean isIAdjective() {
        return isIAdjective;
    }

    public void setIAdjective(boolean IAdjective) {
        isIAdjective = IAdjective;
    }

    public PredicateFormEnum getForm() {
        return form;
    }

    public void setForm(PredicateFormEnum form) {
        this.form = form;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    @Override
    public String toString() {
        return form.name() + "; " + predicate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Predicate predicate1 = (Predicate) o;

        if (form != predicate1.form) return false;
        if (!predicate.equals(predicate1.predicate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = form.hashCode();
        result = 31 * result + predicate.hashCode();
        return result;
    }
}
