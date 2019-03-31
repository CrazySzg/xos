package my.xzq.xos.server.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadParam {

    private Long fileSize;  // 总文件大小
    private String fileName;
    private String targetDir;
    private String category;
    private String chunkMD5; //客户端计算得到的分片MD5值，在服务端重新计算进行比较
    private String uploadId; //  同一个上传任务ID相同
    private Integer partSeq;  //分片序号

}
