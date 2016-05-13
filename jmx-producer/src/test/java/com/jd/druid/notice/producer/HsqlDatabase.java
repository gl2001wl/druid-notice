package com.jd.druid.notice.producer;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Leon Guo
 */
public class HsqlDatabase {

    private static final String CONNECTION_STRING = "jdbc:hsqldb:mem:testdb;shutdown=false";
    private static final String USER_NAME = "SA";
    private static final String PASSWORD = "";
    private static final Log LOG = LogFactory.getLog(HsqlDatabase.class);

    private DruidDataSource dataSource;

    public void init() throws SQLException {
        dataSource = new DruidDataSource();
        dataSource.setUrl(CONNECTION_STRING);
        dataSource.setUsername(USER_NAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");

        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(10);

        dataSource.setMaxWait(60000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);

        dataSource.setTestWhileIdle(false);

        try {
            dataSource.setFilters("stat");
            dataSource.init();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
