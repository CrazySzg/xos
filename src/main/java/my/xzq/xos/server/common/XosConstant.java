package my.xzq.xos.server.common;

public interface XosConstant {

    //接口变量默认是public static final的

    Integer UNEXPECTED_ERROR = -100000;

    Integer SUCCESS = 200;

    String SEPARATOR = "/";


    //注册用户失败,用户名已存在
    Integer REGISTER_USER_FAIL = 101;
    //hbase创建表失败
    Integer CREATE_HBASE_FAIL = 102;
    //hbase删除表失败
    Integer REMOVE_HBASE_FAIL = 103;
    //hbase删除列族失败
    Integer DELETE_HBASE_COLUMN_FAMILY_FAIL = 104;
    //hbase删除列标识失败
    Integer DELETE_HBASE_COLUMN_QUALIFIER_FAIL = 105;
    //hbase删除行失败
    Integer DELETE_HBASE_ROW_FAIL = 106;
    //hbase内部错误
    Integer CONNECT_HBASE_ERROR = 107;
    //hbase删除列失敗
    Integer GET_HBASE_ROW_DATA_FAIL = 108;
    //hbase添加列數據
    Integer PUT_HBASE_ROW_DATA_FAIL = 109;
    //hbase批量添加行失败
    Integer BATCH_PUT_HBASE_ROW_FAIL = 110;
    //保存文件失败(创建目录失败)
    Integer CREATE_HDFS_DIR_FAIL = 111;
}
