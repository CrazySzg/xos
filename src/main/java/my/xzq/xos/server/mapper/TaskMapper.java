package my.xzq.xos.server.mapper;

import my.xzq.xos.server.model.UploadTask;
import org.apache.ibatis.annotations.Param;

public interface TaskMapper {

    Integer createUploadTask(@Param("uploadId")String uploadId, @Param("fileName")String fileName,
                             @Param("totalChunk")Integer totalChunk, @Param("md5List") String md5List);

    Integer updateTaskChunk(@Param("uploadId") String uploadId);

    Integer updateTaskStatus(@Param("uploadId") String uploadId,@Param("status") Integer status);

    UploadTask getUploadInfo(@Param("uploadId") String uploadId);
}
