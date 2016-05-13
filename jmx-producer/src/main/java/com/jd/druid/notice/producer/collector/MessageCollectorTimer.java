package com.jd.druid.notice.producer.collector;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.jd.druid.notice.producer.comparator.ValueComparator;
import com.jd.druid.notice.producer.jmx.DruidStatNotice;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Leon Guo
 */
public class MessageCollectorTimer {

    private static final Log LOG = LogFactory.getLog(MessageCollectorTimer.class);

    private static final long DELAY = 30000;

    private Map<String, List<ValueComparator>> monitorItems;

    public MessageCollectorTimer(Map<String, List<ValueComparator>> monitorItems) {
        this.monitorItems = monitorItems;
    }

    public void start(long period) {
        //Start sender task as daemon thread.
        final Thread senderThread = new DruidStatNotice.NoticeSenderTask();
        senderThread.setDaemon(true);
        senderThread.start();

        Timer timer = new Timer(true);

        TimerTask collectTask = new TimerTask() {
            @Override
            public void run() {
                DruidMessageCollector collector = new DruidMessageCollector();
                for (Map.Entry<String, List<ValueComparator>> entry : monitorItems.entrySet()) {
                    try {
                        List<NoticeMsg> msgFromSource = collector.getMsgFromSource(entry.getKey(), entry.getValue());
                        if (CollectionUtils.isEmpty(msgFromSource)) {
                            continue;
                        }
                        DruidStatNotice.getInstance().addNoticeMsgs(msgFromSource);
                        synchronized (senderThread) {
                            senderThread.notify();
                        }
                    } catch (Throwable e) {
                        LOG.error("Collect or send messages failed: " + e.getMessage(), e);
                    }
                }
            }
        };

        timer.schedule(collectTask, DELAY, period);
    }

}


