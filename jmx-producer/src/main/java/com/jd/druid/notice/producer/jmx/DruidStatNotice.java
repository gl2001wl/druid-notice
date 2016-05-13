package com.jd.druid.notice.producer.jmx;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.jd.druid.notice.producer.collector.NoticeMsg;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Leon Guo
 */
public class DruidStatNotice extends NotificationBroadcasterSupport implements DruidStatNoticeMXBean {

    private static final Log LOG = LogFactory.getLog(DruidStatNotice.class);

    public static final String MBEAN_NAME = "com.jd.druid.notice.producer:type=DruidStatNotice";

    private static DruidStatNotice instance = null;

    //check msg every 5 minute
    private static final long WAIT_TIMEOUT = 5 * 60 * 1000;

    private Queue<NoticeMsg> msgQueue;

    private long sequenceNumber = 1;

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[]{
                AttributeChangeNotification.ATTRIBUTE_CHANGE
        };
        String name = AttributeChangeNotification.class.getName();
        String description = "The notice sent from Druid.";
        MBeanNotificationInfo info =
                new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[]{info};
    }

    private DruidStatNotice(int msgQueueSize) {
        msgQueue = new ArrayBlockingQueue<>(msgQueueSize);
    }

    public static DruidStatNotice getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Current MXBean has not been registered, please invoke 'registerMBean()' to register it first.");
        }
        return instance;
    }

    public static void registerMBean(int msgQueueSize) {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {

            ObjectName objectName = new ObjectName(MBEAN_NAME);
            if (!mbeanServer.isRegistered(objectName)) {
                instance = new DruidStatNotice(msgQueueSize);
                mbeanServer.registerMBean(instance, objectName);
            }
        } catch (JMException ex) {
            LOG.error("register mbean error", ex);
        }
    }

    public void addNoticeMsgs(Collection<NoticeMsg> noticeMsgs) {
        msgQueue.addAll(noticeMsgs);
    }

    @Override
    public long getMsgsSize() {
        return msgQueue.size();
    }

    /**
     *  Thread for send notifications, keep waiting when no msg in queue, will be notified after add msgs to queue.
     */
    public static class NoticeSenderTask extends Thread {

        @Override
        public void run() {
            DruidStatNotice statNotice = getInstance();
            try {
                while (true) {
                    while (statNotice.msgQueue.isEmpty()) {
                        synchronized (this) {
                            wait(WAIT_TIMEOUT);
                        }
                    }
                    statNotice.sendNotice();
                }
            } catch (Throwable e) {
                LOG.error("Notice Sender Task Interrupted.", e);
            }
        }

    }

    //    @Override
    public void sendNotice() {
        NoticeMsg noticeMsg = msgQueue.poll();
        while (noticeMsg != null) {
            Notification notification = buildNotification(noticeMsg);
            sendNotification(notification);
            noticeMsg = msgQueue.poll();
        }
    }

    private Notification buildNotification(NoticeMsg msg) {
        Notification notification = new AttributeChangeNotification(this, sequenceNumber++, msg.getTimestamp(),
                msg.getMsg(), msg.getValueName(), msg.getType(), msg.getOldValue(), msg.getActualValue());
        return notification;
    }

}
