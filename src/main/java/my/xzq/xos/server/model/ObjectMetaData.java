package my.xzq.xos.server.model;

import lombok.Data;

import java.util.Map;


@Data
public class ObjectMetaData {

    private String path;
    private String fileName;
    private String suffix;
    private long size;

}
