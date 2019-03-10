package my.xzq.xos.server.configuration.durid;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Administrator
 * @create 2019-03-08 13:09
 */
@Data
@ConfigurationProperties("spring.datasource")
@ConditionalOnClass(com.alibaba.druid.pool.DruidDataSource.class)
@Deprecated
public class DruidProperties {

    private int initialSize;
    private int minIdle;
    private int maxActive;
    private long timeBetweenEvictionRunsMillis;
    private long minEvictableIdleTimeMillis;
    private String validationQuery;
    private boolean testWhileIdle;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private int maxPoolPreparedStatementPerConnectionSize;
    private String filters;
    private String connectionProperties;

}
