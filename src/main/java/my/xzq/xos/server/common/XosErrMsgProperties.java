package my.xzq.xos.server.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2019-03-09 14:02
 */
@PropertySource(value = "classpath:errMsg.properties",encoding = "UTF-8")
@ConfigurationProperties
@Component
@Data
public class XosErrMsgProperties {

    private Map<String,String> errMsg = new HashMap<>();

    public String getMessage(Integer errCode) {
        return errMsg.get(String.valueOf(errCode));
    }

}
