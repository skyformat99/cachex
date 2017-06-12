package com.alibaba.cacher.support.hitrate;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author jifang.zjf
 * @since 2017/6/8 下午9:41.
 */
public class H2HitRateMXBeanImpl extends AbstractDBHitRateMXBean {

    public H2HitRateMXBeanImpl() {
        this(System.getProperty("user.home") + "/.h2/cacher");
    }

    public H2HitRateMXBeanImpl(String dbPath) {
        super(dbPath);
    }

    @Override
    protected Supplier<JdbcOperations> operationsSupplier(String dbPath) {
        return () -> {
            // 不使用EmbeddedDatabaseBuilder(), 使之有更多的控制
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:" + dbPath + ";AUTO_SERVER=TRUE;AUTO_RECONNECT=TRUE;AUTO_SERVER=TRUE");
            dataSource.setUsername("cacher");
            dataSource.setPassword("cacher");

            JdbcTemplate template = new JdbcTemplate(dataSource);
            template.execute("CREATE TABLE IF NOT EXISTS t_hit_rate(" +
                    "id BIGINT     IDENTITY PRIMARY KEY," +
                    "pattern       VARCHAR(64) NOT NULL UNIQUE," +
                    "hit_count     BIGINT      NOT NULL     DEFAULT 0," +
                    "require_count BIGINT      NOT NULL     DEFAULT 0," +
                    "version       BIGINT      NOT NULL     DEFAULT 0)");

            return template;
        };
    }

    @Override
    protected Stream<DataDO> processMapResults(List<Map<String, Object>> mapResults) {
        return mapResults.stream().map((map) -> {
            AbstractDBHitRateMXBean.DataDO dataDO = new AbstractDBHitRateMXBean.DataDO();
            dataDO.setPattern((String) map.get("PATTERN"));
            dataDO.setHitCount((long) map.get("HIT_COUNT"));
            dataDO.setRequireCount((long) map.get("REQUIRE_COUNT"));
            dataDO.setVersion((long) map.get("VERSION"));

            return dataDO;
        });
    }
}
