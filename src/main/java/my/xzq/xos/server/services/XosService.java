package my.xzq.xos.server.services;


import my.xzq.xos.server.common.response.XosSuccessResponse;
import my.xzq.xos.server.dto.response.UploadResult;
import my.xzq.xos.server.model.XosObject;
import my.xzq.xos.server.model.XosObjectSummary;
import my.xzq.xos.server.model.ObjectListResult;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;


public interface XosService {

    public void createBucketStore(String bucket) throws Exception;

    public void deleteBucketStore(String bucket) throws Exception;

    public void createSeqTable() throws Exception;

    public XosSuccessResponse<UploadResult> create(String uploadId, Long partSeq, String bucket, String dir, String fileName, ByteBuffer content, long size, String category) throws Exception;

    public String putDir(String bucket, String parent, String newDirName) throws Exception;

    public XosObjectSummary getSummary(String bucket, String key, boolean isDir) throws Exception;

    public ObjectListResult listDir(String bucket, String dir) throws Exception;

    public XosObject getObject(String bucket, String key, String fileName) throws Exception;

    public void deleteDir(String bucket, String dir) throws Exception;

    public void deleteObject(String bucket, String dir, String objectName) throws Exception;

    public String createUploadTask(String fileName, List<String> md5List) throws Exception;
}
