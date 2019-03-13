package my.xzq.xos.server;

import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

/**
 * @author Administrator
 * @create 2019-03-12 14:17
 */
public class StringTest {

    @Test
    public void test1() {
        String dir = "/x/y/z/";
        String a = dir.substring(0, dir.lastIndexOf("/") + 1 );
        System.out.println(a);
        System.out.println(a.substring(a.lastIndexOf("/")));

        String b = "aa";
        System.out.println(b.substring(0,b.lastIndexOf("/"))); //-1
    }


    @Test
    public void test2() {
        long a = 256;
        System.out.println(Bytes.toBytes(a));
    }
}
