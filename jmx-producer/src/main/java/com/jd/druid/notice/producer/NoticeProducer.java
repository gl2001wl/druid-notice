package com.jd.druid.notice.producer;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.jd.druid.notice.producer.collector.MessageCollectorTimer;
import com.jd.druid.notice.producer.conf.Configuration;
import com.jd.druid.notice.producer.jmx.DruidStatNotice;

/**
 * @author Leon Guo
 */
public class NoticeProducer {

    private static final Log LOG = LogFactory.getLog(NoticeProducer.class);

    public void start() {
        LOG.info("Starting Druid Notice Producer...");
        Configuration conf = Configuration.load();
        if (conf == null) {
            return;
        }
        LOG.info("Loaded Configuration for Druid Notice Producer successful.");
        MessageCollectorTimer timer = new MessageCollectorTimer(conf.getValueComparators());

        DruidStatNotice.registerMBean(conf.getMsgQueueSize());
        LOG.info("Registered MBean for Druid Notice Producer successful.");

        timer.start(conf.getPeriod());
        LOG.info("Started timer for Druid Notice Producer successful.");
        LOG.info("Druid Notice Producer started.");
    }

}
