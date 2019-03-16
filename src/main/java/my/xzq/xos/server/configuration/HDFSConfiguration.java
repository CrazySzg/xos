package my.xzq.xos.server.configuration;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * @author Administrator
 * @create 2019-03-11 20:42
 */
@Configuration
public class HDFSConfiguration {

    @Value("${xos.hadoop.file-system.user:hadoop}")
    private String user;

    @Value("${xos.hadoop.file-system.url}")
    private String url;

    @Bean("hadoopFileSystem")
    public FileSystem hadoopFileSystem() throws Exception {
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
       // conf.addResource(new Path(ClassLoader.getSystemResource("hdfs/core-site.xml").toURI()));
       // conf.addResource(new Path(ClassLoader.getSystemResource("hdfs/hdfs-site.xml").toURI()));
        // System.setProperty("HADOOP_USER_NAME","hadoop");

        return FileSystem.get(URI.create(url),conf,user);
    }
}
