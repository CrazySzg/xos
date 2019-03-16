package my.xzq.xos.server;

import my.xzq.xos.server.services.XosService;
import my.xzq.xos.server.utils.HbaseUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan(basePackages = "my.xzq.xos.server.mapper")
public class XosServerApplication implements CommandLineRunner {

    @Autowired
    private XosService xosService;

    public static void main(String[] args) {
        SpringApplication.run(XosServerApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        xosService.createSeqTable();
    }
}
