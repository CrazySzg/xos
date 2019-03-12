package my.xzq.xos.server.utils;

import java.util.UUID;

/**
 * @author Administrator
 * @create 2019-03-11 20:02
 */
public class UUIDUtil {

    public static String getUUIDString() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
