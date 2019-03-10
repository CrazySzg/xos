package my.xzq.xos.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan(basePackages = "my.xzq.xos.server.mapper")
public class XosServerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(XosServerApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {

    }
}
