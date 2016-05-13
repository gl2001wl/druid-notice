package com.jd.druid.notice.producer.collector;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Leon Guo
 */
public class NoticeMsg  implements Serializable {

    private String hostName;

    private String dataSourceUrl;

    private String source;

    private String type;

    private String valueName;

    private Long threshold;

    private Long oldValue = 0L;

    private Long actualValue;

//    private Long seq;

    private long timestamp;

    private String fullKey;

    private String msg;

//    private Map<String, Object> sourceObj;

    public NoticeMsg(){}

    @ConstructorProperties({ "hostName", "dataSourceUrl", "source", "type", "valueName", "threshold", "oldValue", "actualValue", "timestamp", "fullKey", "msg" })
    public NoticeMsg(String hostName, String dataSourceUrl, String source, String type, String valueName, Long threshold, Long oldValue, Long actualValue, long timestamp, String fullKey, String msg) {
        this.hostName = hostName;
        this.dataSourceUrl = dataSourceUrl;
        this.source = source;
        this.type = type;
        this.valueName = valueName;
        this.threshold = threshold;
        this.oldValue = oldValue;
        this.actualValue = actualValue;
//        this.seq = seq;
        this.timestamp = timestamp;
        this.fullKey = fullKey;
        this.msg = msg;
//        this.sourceObj = sourceObj;
    }

    public void buildMsg(Map<String, Object> statMap) {
        StringBuilder msg = new StringBuilder(String.format("%s in %s at %s has threshold: %s, but now the value is: %s", fullKey, dataSourceUrl, hostName, threshold, actualValue));
        if (oldValue != null) {
            msg.append(", old value is: ").append(oldValue);
        }
        if (statMap.containsKey("ID")) {
            msg.append(", ID: ").append(statMap.get("ID"));
        }
        this.msg = msg.toString();
    }

    public String getMsg() {
        return msg;
    }

    public String getDataSourceUrl() {
        return dataSourceUrl;
    }

    public void setDataSourceUrl(String dataSourceUrl) {
        this.dataSourceUrl = dataSourceUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getThreshold() {
        return threshold;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
    }

    public Long getActualValue() {
        return actualValue;
    }

    public void setActualValue(Long actualValue) {
        this.actualValue = actualValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

//    public Long getSeq() {
//        return seq;
//    }

//    public void setSeq(Long seq) {
//        this.seq = seq;
//    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFullKey() {
        return fullKey;
    }

    public void setFullKey(String fullKey) {
        this.fullKey = fullKey;
    }

//    public Map<String, Object> getSourceObj() {
//        return sourceObj;
//    }

//    public void setSourceObj(Map<String, Object> sourceObj) {
//        this.sourceObj = sourceObj;
//    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Long getOldValue() {
        return oldValue;
    }

    public void setOldValue(Long oldValue) {
        this.oldValue = oldValue;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
