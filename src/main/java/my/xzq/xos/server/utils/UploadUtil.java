package my.xzq.xos.server.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UploadUtil {

    private static Map<String, List<String>> uploadMd5 = new ConcurrentHashMap<>();

    /**
     * 判断该分片是否为此文件的一部分
     *
     * @param uploadId
     * @param md5
     * @return
     */
    public static boolean isFileChunk(String uploadId, String md5,Long partSeq) {
        return uploadMd5.containsKey(uploadId)
                && uploadMd5.get(uploadId).get(Integer.parseInt(partSeq.toString())).equals(md5);
    }


    /**
     * 上传文件时将文件块md5值存储
     *
     * @param uploadId
     * @param md5List
     */
    public static void putFileMd5List(String uploadId, List<String> md5List) {
        uploadMd5.put(uploadId, md5List);
    }

    /**
     * 完成上传任务或者任务过期将删除缓存，防止内存泄露
     *
     * @param uploadId
     */
    public static void removeFinishUploadTask(String uploadId) {
        uploadMd5.remove(uploadId);
    }


    public static String getChunkMd5(String uploadId,Integer index) {
        return uploadMd5.get(uploadId).get(index);
    }


}
