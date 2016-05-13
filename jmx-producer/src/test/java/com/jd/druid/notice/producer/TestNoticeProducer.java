package com.jd.druid.notice.producer;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.jd.druid.notice.producer.jmx.DruidStatNotice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Leon Guo
 */
public class TestNoticeProducer {

    private static final Log LOG = LogFactory.getLog(TestNoticeProducer.class);

    private HsqlDatabase database;

    @Before
    public void setUp() throws Exception {
        database = new HsqlDatabase();
        database.init();
    }

    @After
    public void tearDown() throws Exception {
        database.close();
    }

    public static class ClientListener implements NotificationListener {
        public void handleNotification(Notification notification,
                                       Object handback) {
            LOG.info("Received notification:");
            LOG.info("ClassName: " + notification.getClass().getName());
            LOG.info("Source: " + notification.getSource());
            LOG.info("Type: " + notification.getType());
            LOG.info("Message: " + notification.getMessage());
            if (notification instanceof AttributeChangeNotification) {
                AttributeChangeNotification acn =
                        (AttributeChangeNotification) notification;
                LOG.info("AttributeName: " + acn.getAttributeName());
                LOG.info("AttributeType: " + acn.getAttributeType());
                LOG.info("NewValue: " + acn.getNewValue());
                LOG.info("OldValue: " + acn.getOldValue());
            }
        }
    }

    @Test
    public void testWithListener() throws InterruptedException, MalformedObjectNameException, InstanceNotFoundException {
        NoticeProducer producer = new NoticeProducer();
        producer.start();

        ClientListener listener = new ClientListener();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbeanName = new ObjectName(DruidStatNotice.MBEAN_NAME);
        mbs.addNotificationListener(mbeanName, listener, null, null);

        while (true) {
            try (Connection conn = database.getConnection(); Statement statement = conn.createStatement();) {
                statement.execute("select abc from not_exist");
            } catch (SQLException e) {
//                LOG.error(e.getMessage(), e);
            }
            Thread.sleep(100);
        }
    }

    @Test
    public void test() throws InterruptedException, MalformedObjectNameException, InstanceNotFoundException {
        NoticeProducer producer = new NoticeProducer();
        producer.start();

        while (true) {
            try (Connection conn = database.getConnection(); Statement statement = conn.createStatement();) {
                statement.execute("select abc from not_exist");
            } catch (SQLException e) {
//                LOG.error(e.getMessage(), e);
            }
            Thread.sleep(100);
        }
    }

}
