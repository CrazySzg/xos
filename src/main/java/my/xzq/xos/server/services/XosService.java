package my.xzq.xos.server.services;


import my.xzq.xos.server.common.response.XosSuccessResponse;
import my.xzq.xos.server.dto.request.DelParam;
import my.xzq.xos.server.dto.response.BreadCrumbs;
import my.xzq.xos.server.dto.response.SearchResult;
import my.xzq.xos.server.dto.response.UploadResponse;
import my.xzq.xos.server.dto.response.UploadResult;
import my.xzq.xos.server.model.ObjectListResult;
import my.xzq.xos.server.model.XosObject;
import my.xzq.xos.server.model.XosObjectSummary;

import java.nio.ByteBuffer;
import java.util.List;


public interface XosService {

    public void createBucketStore(String bucket) throws Exception;

    public void deleteBucketStore(String bucket) throws Exception;

    public void createSeqTable() throws Exception;

    public XosSuccessResponse<UploadResult> create(String uploadId, Integer partSeq, String bucket, String dir, String fileName, ByteBuffer content, long size, String category) throws Exception;

    public void putDir(String bucket, String parent, String newDirName) throws Exception;

    public XosObjectSummary getSummary(String bucket, String key, boolean isDir) throws Exception;

    public ObjectListResult listDir(String bucket, String dir) throws Exception;

    public XosObject getObject(String bucket, String filePath) throws Exception;

    public SearchResult search(String bucket, String keyword) throws Exception;

    public void deleteObject(String bucket, List<DelParam> paths) throws Exception;

    public void rename(String bucket, String rowKey, String oldName, String newName, boolean isDir) throws Exception;

    public UploadResponse createUploadTask(String uploadId, String fileName, List<String> md5List) throws Exception;

    public List<BreadCrumbs> makeBread(String bucket, String path) throws Exception;

    public String preDownload(String bucket, String filePath, long shareTime) throws Exception;

    public boolean validateDownloadToken(String downloadToken, String bucket, String filePath) throws Exception;

    public String getMimeType(String type) throws Exception;

    public void move(String bucket, List<String> paths, String targetDir) throws Exception;

    public ObjectListResult classify(String bucket, String category) throws Exception;
}
