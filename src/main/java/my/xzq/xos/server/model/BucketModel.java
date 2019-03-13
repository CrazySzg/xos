package my.xzq.xos.server.model;


import lombok.Data;
import my.xzq.xos.server.utils.UUIDUtil;

import java.util.Date;


@Data
public class BucketModel {

    private String bucketId;
    private String bucketName;
    private String creator;
    private String detail;
    private Date createTime;

    public BucketModel() {}

    public BucketModel(String bucketName, String creator, String detail) {
        this.bucketId = UUIDUtil.getUUIDString();
        this.bucketName = bucketName;
        this.creator = creator;
        this.detail = detail;
        this.createTime = new Date();
    }
}
