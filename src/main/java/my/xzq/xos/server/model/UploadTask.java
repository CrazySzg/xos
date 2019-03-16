package my.xzq.xos.server.model;

import lombok.Data;

import java.util.Date;

@Data
public class UploadTask {

    private Integer id;
    private String uploadId;
    private String fileName;
    private Integer totalChunk;
    private Integer expectChunk;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
