package com.jd.druid.notice.example.listener;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.jd.druid.notice.producer.conf.Configuration;
import com.jd.druid.notice.producer.jmx.DruidStatNotice;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * @author Leon Guo
 */
public class DruidExampleListener implements NotificationListener {

    private static final Log LOG = LogFactory.getLog(DruidExampleListener.class);

    private Configuration conf;

    public void init() {
        LOG.info("Registering listener for Druid...");
        conf = Configuration.load();
        if (conf == null) {
            return;
        }
        LOG.info("Loaded UMP listener configuration...");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbeanName;
        try {
            mbeanName = new ObjectName(DruidStatNotice.MBEAN_NAME);
            mbs.addNotificationListener(mbeanName, this, null, null);
        } catch (Throwable e) {
            LOG.error("Add UMP listener for UMO failed: " + e.getMessage(), e);
        }
        LOG.info("Registered UMP listener for Druid.");
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (!(notification instanceof AttributeChangeNotification)) {
            return;
        }
        AttributeChangeNotification acn = (AttributeChangeNotification) notification;
        StringBuilder sb = new StringBuilder();
        sb.append("Received notification: ");
        sb.append("ClassName: " + acn.getClass().getName());
        Object sourceObj = acn.getSource();
        sb.append(", Source: " + sourceObj);
        sb.append(", Type: " + acn.getType());
//        sb.append(", Message: " + acn.getMessage());

        String valueName = acn.getAttributeName();
        String type = acn.getAttributeType();
        sb.append(", AttributeName: " + valueName);
        sb.append(", AttributeType: " + type);
        sb.append(", NewValue: " + acn.getNewValue());
        sb.append(", OldValue: " + acn.getOldValue());
        LOG.info(sb.toString());
        LOG.info(acn.getMessage());
    }

}
