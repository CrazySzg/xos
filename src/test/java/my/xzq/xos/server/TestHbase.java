package my.xzq.xos.server;

import my.xzq.xos.server.utils.HbaseUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;

/**
 * @author Administrator
 * @create 2019-03-11 11:32
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestHbase {

    @Autowired
    private HbaseUtil hbaseUtil;

    @Test
    public void test() {
        hbaseUtil.createTable("hbaseutil-test",new String[]{"x","y"});
        hbaseUtil.putRow("hbaseutil-test","util","x","x1","123");
        ResultScanner scanner = hbaseUtil.getScanner("hbaseutil-test");
        Iterator<Result> iterator = scanner.iterator();
        Result result = null;
        while(iterator.hasNext()) {
            result = iterator.next();
            System.out.println(Bytes.toString(result.value()));
        }
        hbaseUtil.deleteColumnQualifier("hbaseutil-test","util","x","x1");
        System.out.println(result.toString());
    }

    @Test
    public void test2() {
        hbaseUtil.putRow("hbaseutil-test","util","x","x1","123");
        ResultScanner scanner = hbaseUtil.getScanner("hbaseutil-test");
        Iterator<Result> iterator = scanner.iterator();
        Result result = null;
        while(iterator.hasNext()) {
            result = iterator.next();
            System.out.println(Bytes.toString(result.value()));
        }
    }

    @Test
    public void test3() {
        hbaseUtil.putRow("hbaseutil-test","util","x","x1","123");
        Result res = hbaseUtil.getRow("hbaseutil-test", "util");
        System.out.println(res.toString());
        System.out.println(hbaseUtil.existRow("hbaseutil-test","util"));
        hbaseUtil.deleteColumnQualifier("hbaseutil-test","util","x","x1");
        System.out.println(hbaseUtil.existRow("hbaseutil-test","util"));
    }

}
