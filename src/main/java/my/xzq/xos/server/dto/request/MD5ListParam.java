package my.xzq.xos.server.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MD5ListParam {

    private String uploadId;
    private String fileName;
    private List<String> checkMd5;
}
