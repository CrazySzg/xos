package my.xzq.xos.server.services.impl;

import my.xzq.xos.server.dto.response.BreadCrumbs;
import my.xzq.xos.server.services.XosService;
import my.xzq.xos.server.utils.JsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class XosServiceImplTest {

    @Autowired
    private XosService xosService;

    @Test
    public void makeBread() throws Exception {
        List<BreadCrumbs> breadCrumbs = xosService.makeBread("3e13ac777fd1451e83b7dcc74994dbb1", "0-4-5-6-");
        System.out.println(JsonUtil.toJson(breadCrumbs));
    }
}