package com.jd.druid.notice.producer.comparator;

/**
 * @author Leon Guo
 */
public class ValueComparator {

    private String valueName;

    private Long compareValue;

    private CompareLogic compareLogic;

    public ValueComparator(String valueName, Long compareValue, CompareLogic compareLogic) {
        this.valueName = valueName;
        this.compareValue = compareValue;
        this.compareLogic = compareLogic;
    }

    public boolean match(Comparable source) {
        return compareLogic.match(source, compareValue);
    }

    public Long getCompareValue() {
        return compareValue;
    }

    public CompareLogic getCompareLogic() {
        return compareLogic;
    }

    public String getValueName() {
        return valueName;
    }

    @Override
    public String toString() {
        return String.format("valueName: %s, compareValue: %s, compareLogic: %s",
                valueName == null ? "null" : valueName,
                compareValue == null ? "null" : compareValue.toString(),
                compareLogic == null ? "null" : compareLogic.toString());
    }

}
