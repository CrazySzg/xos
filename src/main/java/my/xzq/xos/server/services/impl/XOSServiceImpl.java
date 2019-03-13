package my.xzq.xos.server.services.impl;

import lombok.extern.slf4j.Slf4j;
import my.xzq.xos.server.enumeration.Category;
import my.xzq.xos.server.common.XosConstant;
import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.model.XosObject;
import my.xzq.xos.server.model.XosObjectSummary;
import my.xzq.xos.server.model.ObjectListResult;
import my.xzq.xos.server.model.ObjectMetaData;
import my.xzq.xos.server.services.XOSService;
import my.xzq.xos.server.utils.HDFSUtil;
import my.xzq.xos.server.utils.HbaseUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ByteBufferInputStream;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
@Service
public class XOSServiceImpl implements XOSService {

    @Autowired
    private HDFSUtil hdfsUtil;

    @Autowired
    private HbaseUtil hbaseUtil;


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
        Put rootDir = new Put(Bytes.toBytes("/"));
        String seqId = this.makeDirSequenceId(bucket);
        put.addColumn(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_SEQID_QUALIFIER, Bytes.toBytes(seqId));
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
     * 新建文件
     * @param bucket
     * @param dir
     * @param fileName
     * @param content
     * @param size
     * @param category
     * @param isDir
     * @throws Exception
     */
    @Override
    public void put(String bucket, String dir, String fileName, ByteBuffer content, long size, String category, boolean isDir) throws Exception {
        if (!dirExist(bucket, dir)) {
            throw new XosException(XosConstant.CREATE_FILE_FAIL, "directory " + dir + " doesn't exist ");
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new XosException(XosConstant.FILENAME_CAN_NOT_BE_NULL);
        }
        String sequenceId = getDirSequenceId(bucket, dir);

        String fileKey = this.becomeFileKey(sequenceId, fileName);
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
            String fileDir = XosConstant.FILE_STORE_ROOT + XosConstant.SEPARATOR + bucket + XosConstant.SEPARATOR + sequenceId;
            InputStream inputStream = new ByteBufferInputStream(content);
            hdfsUtil.createFile(fileDir, fileName, inputStream, size, (short) 1);
        }
        hbaseUtil.putRow(XosConstant.getObjTableName(bucket), contentPut);
    }


    private String becomeFileKey(String sequenceId, String fileName) {
        return new StringBuilder().append(sequenceId).append("_").append(fileName).toString();
    }

    // 获取文件夹或者文件的属性
    @Override
    public XosObjectSummary getSummary(String bucket, String key, boolean isDir) throws Exception {
        // 判断是否为文件夹
        if (isDir) {
            Result result = hbaseUtil.getRow(XosConstant.getObjTableName(bucket), key);
            if (result != null) {
                // 读取目录的基础属性并转换为XosObjectSummary
                return this.dirObjectToSummary(result);
            }
            return XosObjectSummary.buildEmpty();
        }
        // 获取文件属性
        String dir = key.substring(0, key.lastIndexOf(XosConstant.SEPARATOR) + 1);
        String sequenceId = getDirSequenceId(bucket, dir);
        if (sequenceId == null) {
            return null;
        }
        String fileName = key.substring(key.lastIndexOf(XosConstant.SEPARATOR) + 1);
        String objKey = this.becomeFileKey(sequenceId, fileName);
        Result result = hbaseUtil.getRow(XosConstant.getObjTableName(bucket), objKey);
        if (result == null) {
            return null;
        }
        return this.resultObjectToSummary(result);
    }


    @Override
    public ObjectListResult listDir(String bucket, String dir) throws Exception {
        // 查询目录表
        Get get = new Get(Bytes.toBytes(dir));
        get.addFamily(XosConstant.DIR_SUB_COLUMN_FAMILY_BYTES);

        Result dirResult = hbaseUtil.getRow(XosConstant.getDirTableName(bucket), get);
        List<XosObjectSummary> subDirs = null;
        if (!dirResult.isEmpty()) {
            subDirs = new ArrayList<>();
            for (Cell cell : dirResult.rawCells()) {
                XosObjectSummary xosObjectSummary = new XosObjectSummary();
                byte[] qualifierBytes = new byte[cell.getQualifierLength()];
                CellUtil.copyQualifierTo(cell, qualifierBytes, 0);
                String name = Bytes.toString(qualifierBytes);
                xosObjectSummary.setFileName(name);
                xosObjectSummary.setLastModifyTime(cell.getTimestamp());
                xosObjectSummary.setCategory(Category.DIRECTORY.getType());
                xosObjectSummary.setSize(0);
                xosObjectSummary.setPath(dir + name);
                xosObjectSummary.setDir(true);
                subDirs.add(xosObjectSummary);
            }
        }
        // 查询文件表
        String sequenceId = getDirSequenceId(bucket, dir);
        byte[] objStart = Bytes.toBytes(sequenceId);
        Scan objScan = new Scan();
        objScan.withStartRow(objStart);
        objScan.setRowPrefixFilter(Bytes.toBytes(sequenceId));

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

        listResult.setObjectCount(objectSummaryList.size());
        listResult.setObjectSummaryList(objectSummaryList);

        return listResult;
    }


    /**
     * 下载文件
     * TODO 断点续传
     * @param bucket
     * @param dir
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public XosObject getObject(String bucket, String dir, String fileName) throws Exception {
        String sequenceId = getDirSequenceId(bucket, dir);
        if (sequenceId == null) {
            return null;
        }
        String objKey = this.becomeFileKey(sequenceId, fileName);
        Result result = hbaseUtil.getRow(XosConstant.getObjTableName(bucket), objKey);
        if (result.isEmpty()) {
            return null;
        }
        XosObject xosObject = new XosObject();
        ObjectMetaData objectMetaData = new ObjectMetaData();
        long size = Bytes.toLong(result.getValue(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_SIZE_QUALIFIER));
        objectMetaData.setSize(size);
        objectMetaData.setPath(dir);
        xosObject.setMetaData(objectMetaData);
        // 读取文件内容
        if (result.containsNonEmptyColumn(XosConstant.OBJ_CONTENT_COLUMN_FAMILY_BYTES, XosConstant.OBJ_CONTENT_QUALIFIER)) {
            // 从HBase中读取
            ByteArrayInputStream inputStream = new ByteArrayInputStream(result.getValue(XosConstant.OBJ_CONTENT_COLUMN_FAMILY_BYTES, XosConstant.OBJ_CONTENT_QUALIFIER));
            xosObject.setContent(inputStream);
        } else {
            // 从HDFS中读取
            String fileDir = XosConstant.FILE_STORE_ROOT + XosConstant.SEPARATOR + bucket + XosConstant.SEPARATOR + sequenceId;
            InputStream inputStream = this.hdfsUtil.openFile(fileDir, fileName);
            xosObject.setContent(inputStream);
        }
        return xosObject;
    }

    /**
     * 删除文件或文件夹
     * @param bucket
     * @param dir
     * @param isDir
     * @throws Exception
     */
    @Override
    public void deleteObject(String bucket, String dir, boolean isDir) throws Exception {
        // 判断是否为目录
        if (isDir && dir.endsWith(XosConstant.SEPARATOR)) {
            // 判断目录是否为空
            if (!isDirEmpty(bucket, dir)) {
                throw new XosException(XosConstant.DIR_NOT_EMPTY, "dir is not empty");
            }

            // 从父目录删除数据
            String dir1 = dir.substring(0, dir.lastIndexOf(XosConstant.SEPARATOR));
            String dirName = dir1.substring(dir.lastIndexOf(XosConstant.SEPARATOR) + 1);
            if (StringUtils.hasText(dirName)) {
                String parentDir = dir.substring(0, dir.lastIndexOf(dirName)); // 父目录名称
                hbaseUtil.deleteColumnQualifier(XosConstant.getDirTableName(bucket), parentDir, XosConstant.DIR_SUB_COLUMN_FAMILY, dirName);
            }
            // 从目录表中删除
            hbaseUtil.deleteRow(XosConstant.getDirTableName(bucket), dir);

        } else {
            // 获取文件的length，判断是存在HBase还是HDFS
            String parentDir = dir.substring(0, dir.lastIndexOf(XosConstant.SEPARATOR) + 1);
            String fileName = dir.substring(dir.lastIndexOf(XosConstant.SEPARATOR) + 1);
            String seqId = getDirSequenceId(bucket, parentDir);
            String objKey = this.becomeFileKey(seqId,fileName);
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
                String fileDir = XosConstant.FILE_STORE_ROOT + XosConstant.SEPARATOR + bucket + XosConstant.SEPARATOR + seqId;
                this.hdfsUtil.deleteFile(fileDir, fileName);
            } else {
                // 从HBase删除
                hbaseUtil.deleteRow(XosConstant.getObjTableName(bucket), objKey);
            }
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
     * @param bucket
     * @param parent
     * @param newDirName
     * @return
     * @throws Exception
     */
    @Override
    public String putDir(String bucket, String parent, String newDirName) throws Exception {
        //父目录不存在
        if (!dirExist(bucket, parent)) {
            throw new XosException(XosConstant.PARENT_DIR_NOT_EXIST);
        }
        String fullDir = parent + newDirName + XosConstant.SEPARATOR;
        if (dirExist(bucket, fullDir)) {
            //已存在 TODO
            throw new XosException(XosConstant.DIR_ALREADY_EXIST);
        }
        if (StringUtils.hasText(newDirName)) {
            //添加到父目录的sub列族中
            Put put = new Put(Bytes.toBytes(parent));
            put.addColumn(XosConstant.DIR_SUB_COLUMN_FAMILY_BYTES, Bytes.toBytes(newDirName), Bytes.toBytes(1));
            hbaseUtil.putRow(XosConstant.getDirTableName(bucket), put);
        } else {
            throw new XosException(XosConstant.NEW_DIR_NAME_CANNOT_BE_NULL);
        }
        //计算seqId
        String seqId = makeDirSequenceId(bucket);
        Put dirPut = new Put(Bytes.toBytes(fullDir));
        dirPut.addColumn(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_NAME_QUALIFIER, Bytes.toBytes(newDirName));
        dirPut.addColumn(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_SEQID_QUALIFIER, Bytes.toBytes(seqId));
        hbaseUtil.putRow(XosConstant.getDirTableName(bucket), dirPut);
        return seqId;

    }

    private String makeDirSequenceId(String bucket) throws Exception {
        long incrementColumnValue = hbaseUtil.incrementColumnValue(XosConstant.BUCKET_DIR_SEQ_TABLE, bucket, XosConstant.BUCKET_DIR_SEQ_COLUMN_FAMILY_BYTES, XosConstant.BUCKET_DIR_SEQ_COLUMN_QUALIFIER_BYTES, 1);
        return String.format("%d_%d", incrementColumnValue % 64, incrementColumnValue);
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
        xosObjectSummary.setLastModifyTime(result.rawCells()[0].getTimestamp());
        xosObjectSummary.setSize(0);
        xosObjectSummary.setCategory(Category.DIRECTORY.getType());

        String fileName = Bytes.toString(result.getValue(XosConstant.DIR_META_COLUMN_FAMILY_BYTES, XosConstant.DIR_NAME_QUALIFIER));
        xosObjectSummary.setFileName(fileName);

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
        xosObjectSummary.setLastModifyTime(result.rawCells()[0].getTimestamp());

        String path = new String(result.getRow());
        xosObjectSummary.setPath(path);

        String name = path.split("_", 2)[1];
        xosObjectSummary.setFileName(name);
        xosObjectSummary.setCategory(Bytes.toString(result.getValue(XosConstant.OBJ_META_COLUMN_FAMILY_BYTES, XosConstant.OBJ_CATEGORY_QUALIFIER)));

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
        return listDir(bucket, dir).getObjectSummaryList().size() == 0;
    }
}
