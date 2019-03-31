package my.xzq.xos.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;


@Data
public class XosObjectSummary implements Comparable<XosObjectSummary> {


    //对应hbase中的row，即为文件路径path 如：/x/y/
    private String path;
    // 文件名称
    private String name;
    // 文件大小 文件夹才有大小，文件没有
    private long size;
    // 文件类型
    private String category;
    // 最后修改时间
    private Date updateTime;
    // 创建时间
    private Date createTime;
    // 是否为文件夹
    private boolean isDir;

    private boolean privacy;

    public static XosObjectSummary buildEmpty() {
        return new XosObjectSummary();
    }

    @Override
    public int compareTo(XosObjectSummary that) {
        if(this.isDir && !that.isDir) {
            return 1;
        } else if(!this.isDir && that.isDir) {
            return -1;
        }
        return this.name.compareTo(that.name);
    }
}
