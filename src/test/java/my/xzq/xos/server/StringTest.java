package my.xzq.xos.server;

import org.junit.Test;

/**
 * @author Administrator
 * @create 2019-03-12 14:17
 */
public class StringTest {

    @Test
    public void test1() {
        String dir = "/x/y/z";
        String a = dir.substring(0, dir.lastIndexOf("/"));
        System.out.println(a);
        System.out.println(a.substring(a.lastIndexOf("/")));
    }
}
