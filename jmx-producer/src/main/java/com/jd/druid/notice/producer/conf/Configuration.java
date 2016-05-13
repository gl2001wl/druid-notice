package com.jd.druid.notice.producer.conf;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.jd.druid.notice.producer.comparator.CompareLogic;
import com.jd.druid.notice.producer.comparator.ValueComparator;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leon Guo
 */
public class Configuration {

    private static final Log LOG = LogFactory.getLog(Configuration.class);

    private static final String PROP_KEY = "druid.notice.conf";

    private static final String DEF_CONF_PATH = "/druidnotice.yaml";

    //default is 2 minutes
    private static final long DEF_PERIOD = 1000 * 60 * 2;

    private static final int DEF_MSG_QUEUE_SIZE = Integer.MAX_VALUE;

    private long period;

    private int msgQueueSize;

    private Map<String, List<ValueComparator>> valueComparators;

    private Map<String, Map> ruleParams = new HashMap<>();

    private Configuration() {
    }

    /**
     *  Inner class for Singleton
     */
    private static final class ConfigurationInner {
        private static Configuration conf =  new Configuration();

        static {
            String confPath = System.getProperty(PROP_KEY, DEF_CONF_PATH);
            try (InputStream in = conf.getClass().getResourceAsStream(confPath);
                 Reader fileReader = new InputStreamReader(in)) {
                YamlReader reader = new YamlReader(fileReader);
                Map map = (Map) reader.read();
                conf.period = map.containsKey("period") ? NumberUtils.toLong(map.get("period").toString()) : DEF_PERIOD;
                conf.msgQueueSize = map.containsKey("msgQueueSize") ? NumberUtils.toInt(map.get("msgQueueSize").toString()) : DEF_MSG_QUEUE_SIZE;
                if (map.containsKey("monitorItems")) {
                    conf.valueComparators = new HashMap<>();
                    List monitorItems = (List) map.get("monitorItems");
                    for (Object obj : monitorItems) {
                        loadMonitorItem(conf, (Map) obj);
                    }
                }
            } catch (IOException e) {
                LOG.error("load config file failed.", e);
                conf = null;
            }
        }
    }

    public static Configuration load() {
        //just load once
        return ConfigurationInner.conf;
    }

    private static void loadMonitorItem(Configuration conf, Map monitorItem) {
        if (!monitorItem.containsKey("itemKey")) {
            return;
        }
        String itemKey = monitorItem.get("itemKey").toString();
        conf.valueComparators.put(itemKey, new ArrayList<ValueComparator>());
        if (monitorItem.containsKey("rules")) {
            List rules = (List) monitorItem.get("rules");
            for (Object obj : rules) {
                loadRule(conf, itemKey, (Map) obj);
            }
        }
    }

    private static void loadRule(Configuration conf, String itemKey, Map rule) {
        if (rule.containsKey("valueName") && rule.containsKey("compareValue") && rule.containsKey("logic")) {
            CompareLogic compareLogic = CompareLogic.getLogic(rule.get("logic").toString());
            if (compareLogic != null) {
                ValueComparator valueComparator = new ValueComparator(rule.get("valueName").toString(),
                        NumberUtils.toLong(rule.get("compareValue").toString()), compareLogic);
                conf.valueComparators.get(itemKey).add(valueComparator);
            }
        }
        if (rule.containsKey("params")) {
            Object params = rule.get("params");
            if (params instanceof Map) {
                conf.ruleParams.put(itemKey + "." + rule.get("valueName").toString(), (Map) params);
            }
        }
    }

    public long getPeriod() {
        return period;
    }

    public Map<String, List<ValueComparator>> getValueComparators() {
        return valueComparators;
    }

    public Map<String, Map> getRuleParams() {
        return ruleParams;
    }

    public int getMsgQueueSize() {
        return msgQueueSize;
    }
}
