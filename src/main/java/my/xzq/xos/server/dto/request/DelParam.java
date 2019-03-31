package my.xzq.xos.server.dto.request;

import lombok.Data;

@Data
public class DelParam {

    private String path;
    private boolean dir;
    private String name;
}
