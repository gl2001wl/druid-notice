package com.jd.druid.notice.producer.collector;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.jd.druid.notice.producer.comparator.ValueComparator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author Leon Guo
 */
public class DruidMessageCollector {

    private static final Log LOG = LogFactory.getLog(DruidMessageCollector.class);

//    private MBeanServer mBeanServer;

    private static DruidStatManagerFacade statManagerFacade = DruidStatManagerFacade.getInstance();

//    private static Map<String, Long> NOTICE_SEQ = new HashMap<>();

    private static Map<String, Long> HOLD_VALUES = new HashMap<>();

//    public <T extends Comparable> List<NoticeMsg> getMsg(ValueComparator<T> comparator, String type, String valueName) {
//        return null;
//    }

    private Set<String> accumulateValues;

    private static final Set<String> DEFAULT_ACCUMULATE_VALUES = new HashSet<>();

    static {
        DEFAULT_ACCUMULATE_VALUES.add("datasource.QueryTimeout");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.TransactionQueryTimeout");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.LoginTimeout");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.NotEmptyWaitCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.NotEmptyWaitMillis");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.LogicConnectCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.LogicCloseCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.LogicConnectErrorCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.PhysicalConnectCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.PhysicalCloseCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.PhysicalConnectErrorCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.ExecuteCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.ErrorCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.CommitCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.RollbackCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.PSCacheAccessCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.PSCacheHitCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.PSCacheMissCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.StartTransactionCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.ClobOpenCount");
        DEFAULT_ACCUMULATE_VALUES.add("datasource.BlobOpenCount");

        DEFAULT_ACCUMULATE_VALUES.add("sql.ExecuteCount");
        DEFAULT_ACCUMULATE_VALUES.add("sql.TotalTime");
        DEFAULT_ACCUMULATE_VALUES.add("sql.MaxTimespan");
        DEFAULT_ACCUMULATE_VALUES.add("sql.EffectedRowCount");
        DEFAULT_ACCUMULATE_VALUES.add("sql.FetchRowCount");
        DEFAULT_ACCUMULATE_VALUES.add("sql.ConcurrentMax");
        DEFAULT_ACCUMULATE_VALUES.add("sql.ErrorCount");
    }

    public DruidMessageCollector() {
        accumulateValues = new HashSet<>(DEFAULT_ACCUMULATE_VALUES);
    }

    public DruidMessageCollector(Set<String> accumulateValues) {
        this.accumulateValues = accumulateValues;
    }

    public List<NoticeMsg> getMsgFromSource(String type, List<ValueComparator> comparators) {
        if (statManagerFacade == null) {
            return null;
        }
        List<NoticeMsg> noticeMsgs = new ArrayList<>();
        if ("datasource".equalsIgnoreCase(type)) {
            List<Map<String, Object>> dataSourceStatDataList = statManagerFacade.getDataSourceStatDataList();
            noticeMsgs.addAll(getMsgFromMap(null, comparators, type, dataSourceStatDataList));
        } else if ("sql".equalsIgnoreCase(type)) {
            List<Map<String, Object>> dataSourceStatDataList = statManagerFacade.getDataSourceStatDataList();
            if (CollectionUtils.isEmpty(dataSourceStatDataList)) {
                return null;
            }
            for (Map<String, Object> dataSourceStat : dataSourceStatDataList) {
                Integer identity = NumberUtils.toInt(dataSourceStat.get("Identity").toString());
                String url = (String) dataSourceStat.get("URL");
                List<Map<String, Object>> sqlStatDataList = statManagerFacade.getSqlStatDataList(identity);
                noticeMsgs.addAll(getMsgFromMap(url, comparators, type, sqlStatDataList));
            }
        }
        return noticeMsgs;
    }

    private List<NoticeMsg> getMsgFromMap(String url, List<ValueComparator> comparators, String type,
                                          List<Map<String, Object>> statMapList) {
        List<NoticeMsg> noticeMsgs = new ArrayList<>();
        for (Map<String, Object> statMap : statMapList) {
            for (ValueComparator comparator : comparators) {
                String valueName = comparator.getValueName();
                if (statMap.containsKey(valueName)) {
                    Object value = statMap.get(valueName);
                    try {
                        if (value != null && value instanceof Comparable && comparator.match((Comparable) value)) {
                            Long comparableVal = NumberUtils.toLong(value.toString());
                            String valueFullKey = type.concat(".").concat(valueName);
                            Long oldValue = HOLD_VALUES.get(valueFullKey);
                            if (!updateHoldValue(comparator, comparableVal, valueFullKey)) continue;
//                            addSeq(valueFullKey);
                            buildNoticeMsg(url, type, noticeMsgs, statMap, comparator, valueName, oldValue, comparableVal, valueFullKey);
                        }
                    } catch (Throwable e) {
                        LOG.error("Compare error", e);
                    }
                }
            }
        }
        return noticeMsgs;
    }

    private boolean updateHoldValue(ValueComparator comparator, Long comparableVal, String valueFullKey) {
        if (accumulateValues.contains(valueFullKey)) {
            if (HOLD_VALUES.containsKey(valueFullKey)
                    && !comparator.getCompareLogic().match(comparableVal, HOLD_VALUES.get(valueFullKey))) {
                return false;
            } else {
                HOLD_VALUES.put(valueFullKey, comparableVal);
            }
        }
        return true;
    }

//    private void addSeq(String valueFullKey) {
//        if (NOTICE_SEQ.containsKey(valueFullKey)) {
//            NOTICE_SEQ.put(valueFullKey, NOTICE_SEQ.get(valueFullKey) + 1);
//        } else {
//            NOTICE_SEQ.put(valueFullKey, 1L);
//        }
//    }

    private void buildNoticeMsg(String url, String type, List<NoticeMsg> noticeMsgs, Map<String, Object> statMap, ValueComparator comparator, String valueName, Long oldValue, Long comparableVal, String valueFullKey) {
        NoticeMsg noticeMsg = new NoticeMsg();
        try {
            noticeMsg.setHostName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            // can't get hostname;
            LOG.error("Get hostname failed: " + e.getMessage(), e);
        }
        noticeMsg.setFullKey(valueFullKey);
        noticeMsg.setDataSourceUrl(url == null ? (String) statMap.get("URL") : url);
        noticeMsg.setType(type);
        noticeMsg.setValueName(valueName);
        noticeMsg.setThreshold(comparator.getCompareValue());
        noticeMsg.setOldValue(oldValue);
        noticeMsg.setActualValue(comparableVal);
//        noticeMsg.setSeq(NOTICE_SEQ.get(valueFullKey));
        noticeMsg.setTimestamp(System.currentTimeMillis());
        noticeMsg.buildMsg(statMap);
        noticeMsgs.add(noticeMsg);
    }

}
