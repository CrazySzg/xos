package my.xzq.xos.server;

import my.xzq.xos.server.utils.IdSequenceUtils;
import org.junit.Test;

public class TestIdSeq {


    @Test
    public void test() {
        System.out.println(new IdSequenceUtils().nextId());
    }
}
