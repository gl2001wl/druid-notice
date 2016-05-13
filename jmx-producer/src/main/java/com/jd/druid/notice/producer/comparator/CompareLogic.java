package com.jd.druid.notice.producer.comparator;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author Leon Guo
 */
public abstract class CompareLogic {

    public abstract boolean match(Comparable source, Comparable matcher);

    public static CompareLogic getLogic(String key) {
        if ("GREATER".equalsIgnoreCase(key)) {
            return GREATER;
        }
        if ("LESS".equalsIgnoreCase(key)) {
            return LESS;
        }
        if ("EQUALS".equalsIgnoreCase(key)) {
            return EQUALS;
        }
        return null;
    }

    protected Comparable convert(Comparable matcher, Comparable source) {
        if (matcher instanceof Long) {
            return source instanceof Long ? source : NumberUtils.toLong(source.toString());
        }
        if (matcher instanceof Integer) {
            return source instanceof Integer ? source : NumberUtils.toInt(source.toString());
        }
        if (matcher instanceof String) {
            return source.toString();
        }
        return source;
    }

    public static CompareLogic GREATER = new CompareLogic() {
        public boolean match(Comparable source, Comparable matcher) {
            if (source == null || matcher == null) {
                return false;
            }
            return matcher.compareTo(convert(matcher, source)) < 0;
        }
    };

    public static CompareLogic LESS = new CompareLogic() {
        public boolean match(Comparable source, Comparable matcher) {
            if (source == null || matcher == null) {
                return false;
            }
            return matcher.compareTo(convert(matcher, source)) > 0;
        }
    };

    public static CompareLogic EQUALS = new CompareLogic() {
        public boolean match(Comparable source, Comparable matcher) {
            if (source == null || matcher == null) {
                return false;
            }
            return source.compareTo(convert(matcher, source)) == 0;
        }
    };

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}
