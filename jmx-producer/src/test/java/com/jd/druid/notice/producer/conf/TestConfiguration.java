package com.jd.druid.notice.producer.conf;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import org.junit.Test;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/15
 */
public class TestConfiguration {

    private static final Log LOG = LogFactory.getLog(TestConfiguration.class);

    @Test
    public void test() {
        Configuration conf = Configuration.load();
        LOG.info("period: " + conf.getPeriod());
        LOG.info("comparator: " + conf.getValueComparators());
    }

}
