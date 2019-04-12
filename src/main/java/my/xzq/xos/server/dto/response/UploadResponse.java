package my.xzq.xos.server.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResponse {

    private String uploadId;
    private String dir;
    private String fileName;
    private Integer expectedChunk;
}
