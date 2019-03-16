package my.xzq.xos.server.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MD5ListParam {

    String fileName;
    List<String> checkMd5;
}
