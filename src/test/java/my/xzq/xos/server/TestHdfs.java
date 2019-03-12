package my.xzq.xos.server;

import my.xzq.xos.server.utils.HDFSUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author Administrator
 * @create 2019-03-11 20:32
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestHdfs {

    @Autowired
    private HDFSUtil hdfsUtil;


  /*  @BeforeClass
    private static void before() throws Exception {
        Configuration conf = new Configuration();
        conf.addResource(new Path(ClassLoader.getSystemResource("hdfs/core-site.xml").toURI()));
        conf.addResource(new Path(ClassLoader.getSystemResource("hdfs/hdfs-site.xml").toURI()));
        // System.setProperty("HADOOP_USER_NAME","hadoop");

        fs = FileSystem.get(URI.create("hdfs://bigdata:8020"),conf,"hadoop");
    }*/

    @Test
    public void test1() throws Exception {
        File file = new File("C:\\Users\\Administrator\\Pictures\\16.jpg");
        FileInputStream fileInputStream = new FileInputStream(file);
        hdfsUtil.createFile("/test222","1.jpg",fileInputStream,file.length(),(short)1);
    }


    @Test
    public void test2() throws Exception {
        InputStream inputStream = hdfsUtil.openFile("/test222", "1.jpg");
        FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\Administrator\\Pictures\\1666.jpg"));
        byte[] bytes = new byte[1024];
        int length = 0;
        while((length = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes);
        }
        outputStream.close();
        inputStream.close();
    }


    @Test
    public void test3() throws Exception {
        hdfsUtil.deleteDir("/test222");
    }

    @Test
    public void test4() throws Exception {
        hdfsUtil.deleteFile("/test222","1.jpg");
    }
}
