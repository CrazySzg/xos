package my.xzq.xos.server.dto.request;

import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @create 2019-04-02 13:17
 */
@Data
public class MoveParam {

    // xos_obj 的行键
    private List<String> paths;
    // 目的地
    private String targetDir;

}
