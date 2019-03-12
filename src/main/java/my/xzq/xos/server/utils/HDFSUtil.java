package my.xzq.xos.server.utils;

import lombok.extern.slf4j.Slf4j;
import my.xzq.xos.server.common.XosConstant;
import my.xzq.xos.server.exception.XosException;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Administrator
 * @create 2019-03-11 20:24
 */

@Component
@Slf4j
public class HDFSUtil {

    @Autowired
    @Qualifier("hadoopFileSystem")
    private FileSystem fileSystem;

    @Value("${xos.hadoop.file-system.block-size}")
    private String defaultBlockSize;

    // 当一个文件小于BlockSize的一半的时候，手动将其大小置为BlockSize的一半
    private long initBlockSize;

    private final String SEPARATOR = "/";

    @PostConstruct
    public void afterPropertySet() {
        initBlockSize = Long.valueOf(defaultBlockSize) / 2;
    }

    /**
     * 在hdfs中创建文件
     * @param dir hdfs文件目录
     * @param name hdfs文件名
     * @param inputStream 需要存储的文件流
     * @param length 文件长度
     * @param replication 备份数量
     * @throws Exception
     */
    public void createFile(String dir, String name, InputStream inputStream, long length, short replication) throws Exception {
        this.mkdirs(dir);

        Path path = new Path(dir + SEPARATOR + name);

        long blockSize = length <= initBlockSize ? initBlockSize : Long.valueOf(defaultBlockSize);
        FSDataOutputStream dataOutputStream = this.fileSystem.create(path, true, 512 * 1024, replication, blockSize);

        try {
            fileSystem.setPermission(path, FsPermission.getFileDefault());
            // 开始写入文件
            byte[] buffer = new byte[512 * 1024];
            int len = -1;
            while ((len = inputStream.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            dataOutputStream.close();
        }
    }

    /**
     *
     * @param dir hdfs目录
     * @return
     * @throws Exception
     */
    public boolean mkdirs(String dir) throws Exception {
        Path dirPath = new Path(dir);
        if (!fileSystem.exists(dirPath)) {
            boolean result = fileSystem.mkdirs(dirPath, FsPermission.getDirDefault());
            log.info("create dir " + dir);
            if (!result) {
                throw new XosException(XosConstant.CREATE_HDFS_DIR_FAIL, "create dir " + dir + " error");
            }
            return true;
        }
        return false;
    }

    /**
     *
     * @param dir hdfs文件目录
     * @param name hdfs文件名
     * @return
     * @throws Exception
     */
    public boolean existFile(String dir,String name) throws Exception {
        return this.fileSystem.exists(path(dir,name));
    }

    /**
     *
     * @param dir hdfs文件目录
     * @param name hdfs文件名
     * @return
     * @throws Exception
     */
    public boolean deleteFile(String dir, String name) throws Exception {
        return fileSystem.delete(path(dir,name), false);
    }

    /**
     *
     * @param dir hdfs文件目录
     * @param name hdfs文件名
     * @return
     * @throws Exception
     */
    public InputStream openFile(String dir,String name) throws Exception {
        return fileSystem.open(path(dir,name));
    }

    /**
     * 删除文件夹，如果文件夹中有文件也会被删除
     * @param dir hdfs文件目录
     * @return
     * @throws Exception
     */
    public boolean deleteDir(String dir) throws Exception {
        return fileSystem.delete(new Path(dir),true);
    }

    /**
     *
     * @param dir 目录
     * @param name 文件名
     * @return
     */
    private Path path(String dir,String name) {
        return new Path(dir + SEPARATOR + name);
    }


}
