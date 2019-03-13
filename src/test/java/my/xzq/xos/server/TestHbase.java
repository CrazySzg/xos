package my.xzq.xos.server;

import my.xzq.xos.server.utils.HbaseUtil;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
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
        hbaseUtil.createTable("hbaseutil-test", new String[]{"x", "y"});
        hbaseUtil.putRow("hbaseutil-test", "util", "x", "x1", "123");
        ResultScanner scanner = hbaseUtil.getScanner("hbaseutil-test");
        Iterator<Result> iterator = scanner.iterator();
        Result result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
            System.out.println(Bytes.toString(result.value()));
        }
        hbaseUtil.deleteColumnQualifier("hbaseutil-test", "util", "x", "x1");
        System.out.println(result.toString());
    }

    @Test
    public void test2() {
        hbaseUtil.putRow("hbaseutil-test", "util", "x", "x1", "123");
        ResultScanner scanner = hbaseUtil.getScanner("hbaseutil-test");
        Iterator<Result> iterator = scanner.iterator();
        Result result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
            System.out.println(Bytes.toString(result.value()));
        }
    }

    @Test
    public void test3() {
        hbaseUtil.putRow("hbaseutil-test", "util", "x", "x1", "123");
        hbaseUtil.putRow("hbaseutil-test", "util", "x", "x1", "1234");
        hbaseUtil.putRow("hbaseutil-test", "util", "x", "x2", "123");
        hbaseUtil.putRow("hbaseutil-test", "util", "x", "x3", "123");
        hbaseUtil.putRow("hbaseutil-test", "util", "x", "y", "123");
        hbaseUtil.putRow("hbaseutil-test", "util", "x", "z", "123");
        Get get = new Get("util".getBytes());
      //  get.setFilter(new QualifierFilter(CompareFilter.CompareOp.GREATER_OR_EQUAL, new BinaryComparator("x1".getBytes())));

        Result res = hbaseUtil.getRow("hbaseutil-test", get);
        Cell[] cells = res.rawCells();
       for(Cell cell :cells) {
           byte[] qualifierBytes = new byte[cell.getQualifierLength()];
           CellUtil.copyQualifierTo(cell, qualifierBytes, 0);
           System.out.println(Bytes.toString(qualifierBytes) + cell.getTimestamp());
       }
        System.out.println(res.toString());
        System.out.println(res.getFamilyMap("x".getBytes()));
        System.out.println(hbaseUtil.existRow("hbaseutil-test", "util"));
        hbaseUtil.deleteColumnQualifier("hbaseutil-test", "util", "x", "x1");
        System.out.println(hbaseUtil.existRow("hbaseutil-test", "util"));
    }


    @Test
    public void singleColumnFilterTest() throws Exception {
        /*hbaseUtil.putRow("hbaseutil-test", "util-1", "x", "media", "1");
        hbaseUtil.putRow("hbaseutil-test", "util-2", "x", "media", "1");
        hbaseUtil.putRow("hbaseutil-test", "util-3", "x", "media", "1");
        hbaseUtil.putRow("hbaseutil-test", "util-4", "x", "media", "1");*/
     //   hbaseUtil.putRow("hbaseutil-test", "util-5", "x", "media", "2");
        Scan scan = new Scan();
        //单列值过滤器在发现该行记录并没有你想要比较的列的时候,会把
        //整行数据放入结果集
        SingleColumnValueFilter filter = new SingleColumnValueFilter("x".getBytes(), "media".getBytes(), CompareFilter.CompareOp.EQUAL, "1".getBytes());
        scan.setFilter(filter);
        ResultScanner scanner = hbaseUtil.getScanner("hbaseutil-test", scan);
        Result result = null;
        while((result = scanner.next()) != null) {
            if(result.containsColumn("x".getBytes(), "media".getBytes()))
                System.out.println(Bytes.toString(result.getRow()));
        }
    }

    @Test
    public void rowFilter()  throws Exception {
        /*hbaseUtil.putRow("hbaseutil-test", "util-6", "y", "fileName", "Java");
        hbaseUtil.putRow("hbaseutil-test", "util-7", "y", "fileName", "Core Java");
        hbaseUtil.putRow("hbaseutil-test", "util-8", "y", "fileName", "Java编程思想");
        hbaseUtil.putRow("hbaseutil-test", "util-9", "y", "fileName", "Java核心技术");
        hbaseUtil.putRow("hbaseutil-test", "util-10", "y", "fileName", "Javaaa");
        hbaseUtil.putRow("hbaseutil-test", "util-11", "y", "fileName", "Jaaaa");
        hbaseUtil.putRow("hbaseutil-test", "util-12", "y", "fileName", "java入门到精通");*/
        SingleColumnValueFilter filter = new SingleColumnValueFilter("y".getBytes(), "fileName".getBytes(), CompareFilter.CompareOp.EQUAL,new SubstringComparator("Java") );
        Scan scan = new Scan();
        scan.setFilter(filter);
        ResultScanner scanner = hbaseUtil.getScanner("hbaseutil-test", scan);
        Result result = null;
        while((result = scanner.next()) != null) {
            if(result.containsColumn("y".getBytes(), "fileName".getBytes())) {
                System.out.println(Bytes.toString(result.getValue("y".getBytes(), "fileName".getBytes())));
                System.out.println(Bytes.toString(result.getRow()));
            }
        }
    }

}
