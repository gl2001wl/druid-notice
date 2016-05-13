package com.jd.druid.notice.example.listener;

import com.jd.druid.notice.producer.HsqlDatabase;
import com.jd.druid.notice.producer.NoticeProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Leon Guo
 */
public class TestDruidExampleListener {

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

    @Test
    public void test() throws InterruptedException {
        NoticeProducer producer = new NoticeProducer();
        producer.start();

        DruidExampleListener listener = new DruidExampleListener();
        listener.init();

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
