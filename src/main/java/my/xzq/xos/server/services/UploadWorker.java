package my.xzq.xos.server.services;

import my.xzq.xos.server.mapper.TaskMapper;
import my.xzq.xos.server.model.UploadTask;
import my.xzq.xos.server.utils.HDFSUtil;
import my.xzq.xos.server.utils.HbaseUtil;
import my.xzq.xos.server.utils.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UploadWorker {


    private Lock lock = new ReentrantLock();
    private Condition checkConn = lock.newCondition();
    private Condition incrConn = lock.newCondition();
    private TaskMapper taskMapper;
    private String uploadId;
    private List<String> md5List;
    private HbaseUtil hbaseUtil;
    private HDFSUtil hdfsUtil;


    public UploadWorker(String uploadId, List<String> md5List) {
        this.uploadId = uploadId;
        this.md5List = md5List;
        this.taskMapper = SpringContextUtil.getBean(TaskMapper.class);
        this.hbaseUtil = SpringContextUtil.getBean(HbaseUtil.class);
        this.hdfsUtil = SpringContextUtil.getBean(HDFSUtil.class);
    }


    public void checkAndAppend() {
        try {
            lock.lock();

            Integer expectChunkIndex = 0;
            UploadTask uploadInfo = null;


            Integer expectMd5 = taskMapper.getUploadInfo(uploadId).getExpectChunk();







        } finally {
            lock.unlock();
        }
    }

    public void incrExpectedChunk() {
        try {
            lock.lock();





        } finally {
            lock.unlock();
        }
    }
}
