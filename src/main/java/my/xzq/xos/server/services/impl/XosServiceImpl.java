package my.xzq.xos.server.services.impl;

import lombok.extern.slf4j.Slf4j;
import my.xzq.xos.server.common.XosConstant;
import my.xzq.xos.server.common.response.XosSuccessResponse;
import my.xzq.xos.server.dto.request.DelParam;
import my.xzq.xos.server.dto.response.BreadCrumbs;
import my.xzq.xos.server.dto.response.UploadResponse;
import my.xzq.xos.server.dto.response.UploadResult;
import my.xzq.xos.server.enumeration.Category;
import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.mapper.TaskMapper;
import my.xzq.xos.server.model.*;
import my.xzq.xos.server.services.XosService;
import my.xzq.xos.server.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ByteBufferInputStream;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
@Service
public class XosServiceImpl implements XosService {

    @Autowired
    private HDFSUtil hdfsUtil;

    @Autowired
    private HbaseUtil hbaseUtil;

    @Autowired
    private TaskMapper taskMapper;


    /**
     * 该方法为新建用户的时候调用
     *
     * @param bucket 即为用户名
     */
    @Override
    public void createBucketStore(String bucket) throws Exception {
        // 创建目录表
        hbaseUtil.createTable(XosConstant.getDirTableName(bucket), XosConstant.getDirColumnFamilies());
        // 创建文件表
        hbaseUtil.createTable(XosConstant.getObjTableName(bucket), XosConstant.getObjColumnFamilies());

        // 将其添加到seq表 seq表维护着当前bucket中的最大序列号，序号从0开始
        Put put = new Put(bucket.getBytes());
        put.addColumn(XosConstant.BUCKET_DIR_SEQ_COLUMN_FAMILY_BYTES, XosConstant.BUCKET_DIR_SEQ_COLUMN_QUALIFIER_BYTES, Bytes.toBytes(0L));
        hbaseUtil.putRow(XosConstant.BUCKET_DIR_SEQ_TABLE, put);
        // 目录表中添加根目录
        Put rootDir = new Put(Bytes.toBytes("0-"));

        rootDir.addColumn(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_SEQID_QUALIFIER, Bytes.toBytes("0"));
        // 根目录没有上级目录
        rootDir.addColumn(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_PARENT_QUALIFIER, Bytes.toBytes("-1"));
        hbaseUtil.putRow(XosConstant.getDirTableName(bucket), rootDir);
        // 创建HDFS目录
        this.hdfsUtil.mkdirs(XosConstant.FILE_STORE_ROOT + XosConstant.SEPARATOR + bucket);
    }

    @Override
    public void deleteBucketStore(String bucket) throws Exception {
        // 删除目录表和文件表
        hbaseUtil.removeTable(XosConstant.getDirTableName(bucket));
        hbaseUtil.removeTable(XosConstant.getObjTableName(bucket));
        // 删除seq表中的记录
        hbaseUtil.deleteRow(XosConstant.BUCKET_DIR_SEQ_TABLE, bucket);
        // 删除HDFS上的目录
        hdfsUtil.deleteDir(XosConstant.FILE_STORE_ROOT + XosConstant.SEPARATOR + bucket);
    }

    @Override
    public void createSeqTable() throws Exception {
        hbaseUtil.createTable(XosConstant.BUCKET_DIR_SEQ_TABLE, new String[]{XosConstant.BUCKET_DIR_SEQ_COLUMN_FAMILY});
    }

    /**
     * 上传文件
     *
     * @param uploadId 上传的文件编号
     * @param partSeq  块序号
     * @param bucket   用户uuid
     * @param dir      文件夹 形如 0-1-2-
     * @param fileName 文件名
     * @param content  文件的内容
     * @param size     文件大小
     * @param category 文件类型
     * @return
     * @throws Exception
     */
    @Override
    public XosSuccessResponse<UploadResult> create(String uploadId, Integer partSeq, String bucket, String dir, String fileName, ByteBuffer content, long size, String category) throws Exception {
        String chunkMd5 = DigestUtils.md5DigestAsHex(content.array());
        UploadTask uploadTask = taskMapper.getUploadInfo(uploadId);
        List<String> md5List = JsonUtil.fromJsonList(uploadTask.getMd5List(), String.class);

        if (!md5List.contains(chunkMd5)) {
            // 文件传输过程中有数据丢失，返回的md5和客户端md5不同，客户端重新上传
            return XosSuccessResponse.build(new UploadResult(chunkMd5,partSeq));
        }
        if (!dirExist(bucket, dir)) {
            throw new XosException(XosConstant.CREATE_FILE_FAIL, "directory " + dir + " doesn't exist ");
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new XosException(XosConstant.FILENAME_CAN_NOT_BE_NULL);
        }

        String fileKey = this.becomeFileKey(dir, uploadId);
        if (hbaseUtil.existRow(XosConstant.getObjTableName(bucket), fileKey) || partSeq > 0) {

            String expectMd5 = null;
            Integer expectChunkIndex = 0;
            UploadTask uploadInfo = null;
            do {
                // 前端可以分块同时上传
                uploadInfo = taskMapper.getUploadInfo(uploadId);
                expectChunkIndex = uploadInfo.getExpectChunk();
                expectMd5 = md5List.get(expectChunkIndex);
                // 不相等说明此时完成上传的块还不能拼接上去
            } while (!ObjectUtils.nullSafeEquals(expectMd5, chunkMd5) && sleep());
            if (size <= XosConstant.FILE_STORE_THRESHOLD) {
                // 已存在 ，追加
                Append append = new Append(fileKey.getBytes());
                append.add(XosConstant.OBJ_CONTENT_COLUMN_FAMILY_BYTES, XosConstant.OBJ_CONTENT_QUALIFIER, content.array());
                hbaseUtil.appendRow(XosConstant.getObjTableName(bucket), append);
            } else {
                // 放入HDFS
                String fileDir = XosConstant.FILE_STORE_ROOT + XosConstant.SEPARATOR + bucket + XosConstant.SEPARATOR + dir;
                InputStream inputStream = new ByteBufferInputStream(content);
                hdfsUtil.appendFile(fileDir, uploadId, inputStream, size, (short) 1);
            }
            taskMapper.updateTaskChunk(uploadId);
            if (expectChunkIndex + 1 == uploadInfo.getTotalChunk()) {
                taskMapper.updateTaskStatus(uploadId, XosConstant.UPLOAD_TASK_FINISH);
            }

            return XosSuccessResponse.build(new UploadResult(chunkMd5,expectChunkIndex));
        }
        Put contentPut = new Put(Bytes.toBytes(fileKey));
        // 文件类型
        if (!StringUtils.isEmpty(category)) {
            contentPut.addColumn(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_CATEGORY_QUALIFIER, category.getBytes());
        }
        // 文件大小
        contentPut.addColumn(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_SIZE_QUALIFIER, Bytes.toBytes(size));
        // 文件名
        contentPut.addColumn(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_FILENAME_QUALIFIER, Bytes.toBytes(fileName));
        // 判断文件大小
        if (size <= XosConstant.FILE_STORE_THRESHOLD) {
            // 直接放入HBase
            ByteBuffer byteBuffer = ByteBuffer.wrap(XosConstant.OBJ_CONTENT_QUALIFIER);
            contentPut.addColumn(XosConstant.OBJ_CONTENT_COLUMN_FAMILY_BYTES, byteBuffer, System.currentTimeMillis(), content);
            byteBuffer.clear();
        } else {
            // 放入HDFS
            String fileDir = XosConstant.FILE_STORE_ROOT + XosConstant.SEPARATOR + bucket + XosConstant.SEPARATOR + dir;
            InputStream inputStream = new ByteBufferInputStream(content);
            hdfsUtil.createFile(fileDir, uploadId, inputStream, size, (short) 1);
        }
        hbaseUtil.putRow(XosConstant.getObjTableName(bucket), contentPut);
        taskMapper.updateTaskChunk(uploadId);
        return XosSuccessResponse.build(new UploadResult(chunkMd5,partSeq));
    }


    private boolean sleep() throws Exception {
        Thread.sleep(100);
        return true;
    }

    private String becomeFileKey(String sequenceId, String fileName) {
        return new StringBuilder().append(sequenceId).append("_").append(fileName).toString();
    }


    /**
     * 获取文件夹或者文件的属性
     *
     * @param bucket 用户的uuid
     * @param key    文件 e.g. 0-1-2-_uuid; 文件夹： 0-1-2-
     * @param isDir  是否为文件夹
     * @return
     * @throws Exception
     */
    @Override
    public XosObjectSummary getSummary(String bucket, String key, boolean isDir) throws Exception {
        // 判断是否为文件夹
        if (isDir) {
            Result result = hbaseUtil.getRow(XosConstant.getDirTableName(bucket), key);
            if (result != null) {
                // 读取目录的基础属性并转换为XosObjectSummary
                return this.dirObjectToSummary(result);
            }
            return XosObjectSummary.buildEmpty();
        }

        // 获取文件属性
        Result result = hbaseUtil.getRow(XosConstant.getObjTableName(bucket), key);
        if (result == null) {
            return null;
        }
        return this.resultObjectToSummary(result);
    }


    /**
     * 展示文件列表
     *
     * @param bucket 用户的uuid
     * @param dir    文件夹id 0-1-2-
     * @return
     * @throws Exception
     */
    @Override
    public ObjectListResult listDir(String bucket, String dir) throws Exception {
        // 查询目录表
        Get get = new Get(Bytes.toBytes(dir));
        get.addFamily(XosConstant.DIR_SUB_COLUMN_FAMILY_BYTES);

        Result dirResult = hbaseUtil.getRow(XosConstant.getDirTableName(bucket), get);
        List<XosObjectSummary> subDirs = null;
        if (!dirResult.isEmpty()) {
            subDirs = new ArrayList<>();
            // 封装文件夹对象
            for (Cell cell : dirResult.rawCells()) {
                XosObjectSummary xosObjectSummary = new XosObjectSummary();
                byte[] qualifierBytes = new byte[cell.getQualifierLength()];
                CellUtil.copyQualifierTo(cell, qualifierBytes, 0);
                String name = Bytes.toString(qualifierBytes);
                xosObjectSummary.setName(name);
                xosObjectSummary.setUpdateTime(new Date(cell.getTimestamp()));
                xosObjectSummary.setCategory(Category.DIRECTORY.getType());
                xosObjectSummary.setSize(0);

                String seqId = Bytes.toString(CellUtil.cloneValue(cell));
                xosObjectSummary.setPath(dir + seqId + XosConstant.STUB);
                xosObjectSummary.setDir(true);
                subDirs.add(xosObjectSummary);
            }
        }
        // 查询文件表
        Scan objScan = new Scan();
        objScan.withStartRow(Bytes.toBytes(dir));
        objScan.setRowPrefixFilter(Bytes.toBytes(dir));
        objScan.addFamily(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES);

        ResultScanner scanner = hbaseUtil.getScanner(XosConstant.getObjTableName(bucket), objScan);
        List<XosObjectSummary> objectSummaryList = new ArrayList<>();
        Result result = null;
        while ((result = scanner.next()) != null) {
            XosObjectSummary xosObjectSummary = resultObjectToSummary(result);
            objectSummaryList.add(xosObjectSummary);
        }
        if (scanner != null) {
            scanner.close();
        }
        if (CollectionUtils.isNotEmpty(subDirs)) {
            objectSummaryList.addAll(subDirs);
        }

        Collections.sort(objectSummaryList);

        ObjectListResult listResult = new ObjectListResult();

        listResult.setCount(objectSummaryList.size());
        listResult.setData(objectSummaryList);

        return listResult;
    }


    /**
     * 下载文件
     * TODO 断点续传
     *
     * @param bucket   用户的uuid
     * @param filePath 文件 形如 0-1-2-uuid
     * @return
     * @throws Exception
     */
    @Override
    public XosObject getObject(String bucket, String filePath) throws Exception {
        if (StringUtils.isEmpty(filePath)) {
            throw new XosException(XosConstant.BAD_PARAM);
        }
        String[] res = filePath.split("_", 2);
        String path = res[0];
        String uuid = res[1];

        Result result = hbaseUtil.getRow(XosConstant.getObjTableName(bucket), filePath);
        if (result.isEmpty()) {
            throw new XosException(XosConstant.DOWNLOAD_FAIL);
        }
        XosObject xosObject = new XosObject();
        ObjectMetaData objectMetaData = new ObjectMetaData();
        long size = Bytes.toLong(result.getValue(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_SIZE_QUALIFIER));
        String fileName = Bytes.toString(result.getValue(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_FILENAME_QUALIFIER));
        objectMetaData.setFileName(fileName);
        objectMetaData.setSize(size);
        objectMetaData.setPath(path);

        xosObject.setMetaData(objectMetaData);
        // 读取文件内容
        if (result.containsNonEmptyColumn(XosConstant.OBJ_CONTENT_COLUMN_FAMILY_BYTES, XosConstant.OBJ_CONTENT_QUALIFIER)) {
            // 从HBase中读取
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    result.getValue(XosConstant.OBJ_CONTENT_COLUMN_FAMILY_BYTES, XosConstant.OBJ_CONTENT_QUALIFIER));
            xosObject.setContent(inputStream);
        } else {
            // 从HDFS中读取
            String fileDir = XosConstant.FILE_STORE_ROOT + XosConstant.SEPARATOR + bucket + XosConstant.SEPARATOR + path;
            InputStream inputStream = this.hdfsUtil.openFile(fileDir, uuid);
            xosObject.setContent(inputStream);
        }
        return xosObject;
    }


    /**
     * 删除文件
     *
     * @param bucket     用户的uuid
     *
     * @throws Exception 异常
     */
    @Override
    public void deleteObject(String bucket, List<DelParam> paths) throws Exception {
        if(CollectionUtils.isNotEmpty(paths)) {
            for(DelParam path : paths) {
                // 判断目录是否为空
                String dir = path.getPath();
                if (path.isDir() && !isDirEmpty(bucket, dir)) {
                    throw new XosException(XosConstant.DIR_NOT_EMPTY,"目录：" + path.getName() + "不为空");
                }
            }
            for(DelParam path : paths) {
                if(path.isDir()) {
                    String dir = path.getPath();
                    if (ObjectUtils.nullSafeEquals(dir, "0-")) {
                        throw new XosException(XosConstant.ROOT_DIR_CANNOT_DELETE);
                    }

                    Result deletedRow = hbaseUtil.getRow(XosConstant.getDirTableName(bucket), dir);

                    String parentDir = Bytes.toString(deletedRow.getValue(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_PARENT_QUALIFIER));
                    String dirName = Bytes.toString(deletedRow.getValue(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_NAME_QUALIFIER));
                    // 从父目录删除数据

                    if (StringUtils.hasText(dirName) && StringUtils.hasText(parentDir)) {
                        hbaseUtil.deleteColumnQualifier(XosConstant.getDirTableName(bucket), parentDir, XosConstant.DIR_SUB_COLUMN_FAMILY, dirName);
                    }
                    // 从目录表中删除
                    hbaseUtil.deleteRow(XosConstant.getDirTableName(bucket), dir);
                } else {
                    String objKey = path.getPath();
                    String[] dirAndUuid = path.getPath().split("_",2);

                    Get get = new Get(objKey.getBytes());
                    // 查询长度

                    get.addColumn(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_SIZE_QUALIFIER);
                    Result result = hbaseUtil.getRow(XosConstant.getObjTableName(bucket), get);
                    if (result.isEmpty()) {
                        return;
                    }
                    long size = Bytes.toLong(result.getValue(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_SIZE_QUALIFIER));
                    // TODO 回收站
                    if (size > XosConstant.FILE_STORE_THRESHOLD) {
                        // 从HDFS删除
                        String fileDir = XosConstant.FILE_STORE_ROOT + XosConstant.SEPARATOR + bucket + XosConstant.SEPARATOR + dirAndUuid[0];
                        this.hdfsUtil.deleteFile(fileDir, dirAndUuid[1]);
                    }
                    // 从HBase删除
                    hbaseUtil.deleteRow(XosConstant.getObjTableName(bucket), objKey);
                }
            }
        } else {
            throw new XosException(XosConstant.BAD_PARAM);
        }
    }

    /**
     * @param bucket    用户的uuid
     * @param rowKey 该文件或文件夹所在目录 形如 0-1-2- 0-1-2-_fileUuid
     * @param oldName   原文件或文件夹名称
     * @param newName   新的文件或文件夹名称
     * @throws Exception  异常
     */
    @Override
    public void rename(String bucket, String rowKey, String oldName, String newName, boolean isDir) throws Exception {
        //重命名文件
        if (StringUtils.isEmpty(newName)) {
            throw new XosException(XosConstant.NEW_NAME_CAN_NOT_NULL);
        }
        if (ObjectUtils.nullSafeEquals(oldName, newName)) {
            return;
        }
        if (isDir) {
            //重命名文件夹
            if (hbaseUtil.existRow(XosConstant.getDirTableName(bucket), rowKey)) {
                // 重新设置父目录中的qualifier
                Result renameRow = hbaseUtil.getRow(XosConstant.getDirTableName(bucket), rowKey);

                String parentRowKey = Bytes.toString(renameRow.getValue(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_PARENT_QUALIFIER));

                String seqId = Bytes.toString(renameRow.getValue(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_SEQID_QUALIFIER));

                hbaseUtil.deleteColumnQualifier(XosConstant.getDirTableName(bucket), parentRowKey,
                        XosConstant.DIR_SUB_COLUMN_FAMILY, oldName);

                hbaseUtil.putRow(XosConstant.getDirTableName(bucket), parentRowKey,
                        XosConstant.DIR_SUB_COLUMN_FAMILY, newName, seqId);

                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_NAME_QUALIFIER, Bytes.toBytes(newName));

                hbaseUtil.putRow(XosConstant.getDirTableName(bucket), put);

            }
        } else {
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_FILENAME_QUALIFIER, Bytes.toBytes(newName));
            hbaseUtil.putRow(XosConstant.getObjTableName(bucket), put);
        }

    }


    @Override
    public List<BreadCrumbs> makeBread(String bucket, String path) throws Exception {
        if(!hbaseUtil.existRow(XosConstant.getDirTableName(bucket),path)) {
            throw new XosException(XosConstant.DIR_NOT_EXIST);
        }
        List<BreadCrumbs> breadCrumbs = new ArrayList<>();
        if (ObjectUtils.nullSafeEquals(path, XosConstant.ROOT)) {
            breadCrumbs.add(new BreadCrumbs(XosConstant.ROOT, ""));
            return breadCrumbs;
        }
        List<String> rowKeys = new ArrayList<>();
        rowKeys.add(XosConstant.ROOT);
        String[] seqs = path.split("-");
        int i = 0;
        for (String s : seqs) {
            if (!s.equals("0") && !s.equals("")) {
                String key = rowKeys.get(i) + s + XosConstant.STUB;
                rowKeys.add(key);
                i++;
            }
        }
        // 删除0-，因为根目录没有dirName
        rowKeys.remove(0);

        Result[] rows = hbaseUtil.getRows(XosConstant.getDirTableName(bucket), rowKeys);
        for(Result result : rows) {
            String fileName = Bytes.toString(result.getValue(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_NAME_QUALIFIER));
            String rowKey = Bytes.toString(result.getRow());
            breadCrumbs.add(new BreadCrumbs(rowKey,fileName));
        }

        return breadCrumbs;
    }

    @Override
    @Transactional
    public UploadResponse createUploadTask(String uploadId, String fileName, List<String> md5List) throws Exception {
         if(StringUtils.hasText(uploadId)) {
             // 说明之前有暂停上传过，返回需要的分片
             UploadTask uploadInfo = taskMapper.getUploadInfo(uploadId);
             return UploadResponse.builder()
                     .expectedChunk(uploadInfo.getExpectChunk())
                     .fileName(uploadInfo.getFileName())
                     .uploadId(uploadInfo.getUploadId())
                     .build();
         } else {
            // 首次上传
             uploadId = UUIDUtil.getUUIDString();
             taskMapper.createUploadTask(uploadId , fileName, md5List.size(), JsonUtil.toJson(md5List));
             return UploadResponse.builder()
                     .expectedChunk(0)
                     .uploadId(uploadId)
                     .fileName(fileName)
                     .build();
         }
    }

    private boolean dirExist(String bucket, String dir) {
        return hbaseUtil.existRow(XosConstant.getDirTableName(bucket), dir);
    }

    private String getDirSequenceId(String bucket, String key) {
        Result result = hbaseUtil.getRow(XosConstant.getDirTableName(bucket), key);
        if (result == null) {
            return null;
        }
        return Bytes.toString(result.getValue(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_SEQID_QUALIFIER));
    }

    /**
     * 新建文件夹
     *
     * @param bucket     用户的uuid
     * @param parent     形如 0-1-2-
     * @param newDirName 文件夹名称
     * @return
     * @throws Exception
     */
    @Override
    public void putDir(String bucket, String parent, String newDirName) throws Exception {
        //父目录不存在
        if (!dirExist(bucket, parent)) {
            throw new XosException(XosConstant.PARENT_DIR_NOT_EXIST);
        }
        String sequenceId = this.makeDirSequenceId(bucket);
        String newDir = parent + sequenceId + XosConstant.STUB;
        if (dirExist(bucket, newDir)) {
            //已存在 TODO
            throw new XosException(XosConstant.DIR_ALREADY_EXIST);
        }

        if (StringUtils.hasText(newDirName)) {
            //添加到父目录的sub列族中
            Put put = new Put(Bytes.toBytes(parent));
            put.addColumn(XosConstant.DIR_SUB_COLUMN_FAMILY_BYTES, Bytes.toBytes(newDirName), Bytes.toBytes(sequenceId));
            hbaseUtil.putRow(XosConstant.getDirTableName(bucket), put);
        } else {
            throw new XosException(XosConstant.NEW_DIR_NAME_CANNOT_BE_NULL);
        }

        Put dirPut = new Put(Bytes.toBytes(newDir));
        dirPut.addColumn(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_NAME_QUALIFIER, Bytes.toBytes(newDirName));
        dirPut.addColumn(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_PARENT_QUALIFIER, Bytes.toBytes(parent));
        // 重命名文件夹时候用到
        dirPut.addColumn(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_SEQID_QUALIFIER, Bytes.toBytes(sequenceId));
        hbaseUtil.putRow(XosConstant.getDirTableName(bucket), dirPut);
    }

    private String makeDirSequenceId(String bucket) throws Exception {
        long res = hbaseUtil.incrementColumnValue(XosConstant.BUCKET_DIR_SEQ_TABLE, bucket, XosConstant.BUCKET_DIR_SEQ_COLUMN_FAMILY_BYTES, XosConstant.BUCKET_DIR_SEQ_COLUMN_QUALIFIER_BYTES, 1);
        return String.valueOf(res);
    }

    /**
     * 从目录结果转换得到XOSObjectSummary
     *
     * @param result
     * @return
     */
    private XosObjectSummary dirObjectToSummary(Result result) {
        XosObjectSummary xosObjectSummary = new XosObjectSummary();
        xosObjectSummary.setPath(Bytes.toString(result.getRow()));
        xosObjectSummary.setUpdateTime(new Date(result.rawCells()[0].getTimestamp()));
        xosObjectSummary.setSize(0);
        xosObjectSummary.setCategory(Category.DIRECTORY.getType());

        String fileName = Bytes.toString(result.getValue(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_NAME_QUALIFIER));
        xosObjectSummary.setName(fileName);

        return xosObjectSummary;
    }

    /**
     * 从文件结果转换得到XosObjectSummary
     *
     * @param result
     * @return
     */
    private XosObjectSummary resultObjectToSummary(Result result) {
        XosObjectSummary xosObjectSummary = new XosObjectSummary();
        xosObjectSummary.setUpdateTime(new Date(result.rawCells()[0].getTimestamp()));

        String path = new String(result.getRow());
        xosObjectSummary.setPath(path);

        String name = Bytes.toString(result.getValue(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_FILENAME_QUALIFIER));
        xosObjectSummary.setName(name);
        long size = Bytes.toLong(result.getValue(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_SIZE_QUALIFIER));
        xosObjectSummary.setSize(size);
        xosObjectSummary.setCategory(Bytes.toString(result.getValue(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_CATEGORY_QUALIFIER)));
        xosObjectSummary.setDir(false);
        return xosObjectSummary;
    }

    private void getDirAllFiles(String bucket, String dir, String seqId, List<XosObjectSummary> keys,
                                String endKey) throws IOException {

        byte[] max = Bytes.createMaxByteArray(100);
        byte[] tail = Bytes.add(Bytes.toBytes(seqId), max);
        if (endKey.startsWith(dir)) {
            String endKeyLeft = endKey.replace(dir, "");
            String fileNameMax = endKeyLeft;
            if (endKeyLeft.indexOf("/") > 0) {
                fileNameMax = endKeyLeft.substring(0, endKeyLeft.indexOf("/"));
            }
            tail = Bytes.toBytes(seqId + "_" + fileNameMax);
        }

        Scan scan = new Scan(Bytes.toBytes(seqId), tail);
        scan.setFilter(XosConstant.OBJ_META_SCAN_FILTER);
        ResultScanner scanner = hbaseUtil
                .getScanner(XosConstant.getObjTableName(bucket), scan);
        Result result = null;
        while ((result = scanner.next()) != null) {
            XosObjectSummary summary = this.resultObjectToSummary(result);
            keys.add(summary);
        }
        if (scanner != null) {
            scanner.close();
        }
    }


    public boolean isDirEmpty(String bucket, String dir) throws Exception {
        return listDir(bucket, dir).getData().size() == 0;
    }

    @Override
    public String preDownload(String bucket,String filePath,long shareTime) throws Exception {
        if(hbaseUtil.existRow(XosConstant.getObjTableName(bucket),filePath)) {
            String salt = JWTUtil.generateSalt();
            Put put = new Put(filePath.getBytes());
            put.addColumn(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES,XosConstant.OBJ_DOWNLOAD_SALT_QUALIFIER,salt.getBytes());
            hbaseUtil.putRow(XosConstant.getObjTableName(bucket),put);

            long nowMillis = System.currentTimeMillis();
            long ttlMillis;
            Date ttl = null;
            if(shareTime != -1L)  {
                ttlMillis = nowMillis + shareTime;
                ttl = new Date(ttlMillis);
            }
            Date now  = new Date(nowMillis);

            String token = JWTUtil.generateDownloadToken(bucket, filePath, salt,now,ttl);
            return token;
        }
        throw new XosException(XosConstant.OPERATION_ILLEGAL);
    }

    @Override
    public String getMimeType(String type) throws Exception {
        String mimeType;
        switch (type) {
            case "pdf" :
                mimeType =  "application/pdf";
                break;
            case "doc" :
                mimeType =  "application/msword;application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                break;
            case "ppt" :
                mimeType =  "application/vnd.ms-powerpoint;application/vnd.openxmlformats-officedocument.presentationml.presentation";
                break;
            case "xls" :
                mimeType =  "application/vnd.ms-excel;application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            case "audio" :
                mimeType =  "audio/mpeg;audio/ogg;audio/basic;audio/aac;audio/wav;audio/x-mpegurl;audio/*";
                break;
            case "video" :
                mimeType =  "video/x-msvideo;video/x-flv;video/mpeg;video/quicktime;video/mp4;video/3gpp;video/*";
                break;
            case "image" :
                mimeType =  "image/bmp;image/gif;image/x-icon;image/jpeg;image/png;image/*";
                break;
            default:
                mimeType = "application/json";
        }
        return mimeType;
    }

    @Override
    public boolean validateDownloadToken(String downloadToken,String bucket,String filePath) throws Exception {

        if(StringUtils.hasText(bucket) && StringUtils.hasText(filePath)) {
            Result res = hbaseUtil.getRow(XosConstant.getObjTableName(bucket), filePath);
            String salt = "";
            if(res != null) {
                salt = Bytes.toString(res.getValue(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_DOWNLOAD_SALT_QUALIFIER));
            }
            try {
                return JWTUtil.validateToken(downloadToken,salt);
            } catch (Exception e) {
                e.printStackTrace();
                //TODO 验证失败
                return false;
            }
        }
        return false;
    }
}
