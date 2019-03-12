package my.xzq.xos.server.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Administrator
 * @create 2019-03-11 10:42
 */
@Configuration
@Slf4j
public class HbaseConfiguration {

    @Bean("hbaseConnection")
    public Connection hbaseConnection() throws Exception {
        org.apache.hadoop.conf.Configuration configuration = HBaseConfiguration.create();
        configuration.addResource(new Path(ClassLoader.getSystemResource("hbase/hbase-site.xml").toURI()));
        configuration.addResource(new Path(ClassLoader.getSystemResource("hbase/core-site.xml").toURI()));
        log.info("{init} HBase Connection success init");
        return ConnectionFactory.createConnection(configuration);
    }
}
