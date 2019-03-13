package my.xzq.xos.server.common;

import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

public final class XosConstant {

    public static final Integer UNEXPECTED_ERROR = -100000;

    public static final Integer SUCCESS = 200;

    public static final String SEPARATOR = "/";

    // 目录表前缀
    public final static String DIR_TABLE_PREFIX = "xos_dir_";
    // 对象表前缀
    public final static String OBJ_TABLE_PREFIX = "xos_obj_";

    // 目录表meta信息列族名
    public final static String DIR_META_COLUMN_FAMILY = "meta";
    public final static byte[] DIR_META_COLUMN_FAMILY_BYTES = DIR_META_COLUMN_FAMILY.getBytes();
    // 目录表sub信息列族名
    public final static String DIR_SUB_COLUMN_FAMILY = "sub";
    public final static byte[] DIR_SUB_COLUMN_FAMILY_BYTES = DIR_SUB_COLUMN_FAMILY.getBytes();

    // 文件表meta信息列族名
    public final static String OBJ_META_COLUMN_FAMILY = "meta";
    public final static byte[] OBJ_META_COLUMN_FAMILY_BYTES = OBJ_META_COLUMN_FAMILY.getBytes();
    // 文件表content信息列族名
    public final static String OBJ_CONTENT_COLUMN_FAMILY = "content";
    public final static byte[] OBJ_CONTENT_COLUMN_FAMILY_BYTES = OBJ_CONTENT_COLUMN_FAMILY.getBytes();

    // 目录表seqId列名
    public final static byte[] DIR_SEQID_QUALIFIER = "seqId".getBytes();
    // 目录表对应目录名 用于搜索
    public final static byte[] DIR_NAME_QUALIFIER = "dirName".getBytes();
    // 文件名用于搜索
    public final static byte[] OBJ_FILENAME_QUALIFIER = "fileName".getBytes();
    // 文件表content列名
    public final static byte[] OBJ_CONTENT_QUALIFIER = "content".getBytes();
    // 文件表length列名
    public final static byte[] OBJ_SIZE_QUALIFIER = "size".getBytes();
    // 文件表property列名
    public final static byte[] OBJ_PROPERTY_QUALIFIER = "property".getBytes();
    // 文件表mediatype列名
    public final static byte[] OBJ_CATEGORY_QUALIFIER = "category".getBytes();

    public static final FilterList OBJ_META_SCAN_FILTER = new FilterList(FilterList.Operator.MUST_PASS_ONE);

    static {
        try {
            byte[][] qualifiers = new byte[][]{XosConstant.DIR_SEQID_QUALIFIER,
                    XosConstant.OBJ_SIZE_QUALIFIER,
                    XosConstant.OBJ_CATEGORY_QUALIFIER};
            for (byte[] b : qualifiers) {
                Filter filter = new QualifierFilter(CompareFilter.CompareOp.EQUAL,
                        new BinaryComparator(b));
                OBJ_META_SCAN_FILTER.addFilter(filter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 文件根目录
    public final static String FILE_STORE_ROOT = "/xos";
    // 文件大小阈值 20M
    public final static int FILE_STORE_THRESHOLD = 20 * 1024 * 1024;
    // 存储hbase目录表的seqId的表，协助生成目录的SequenceID的表
    public final static String BUCKET_DIR_SEQ_TABLE = "xos_dir_seq";
    public final static String BUCKET_DIR_SEQ_COLUMN_FAMILY = "seq";
    public final static byte[] BUCKET_DIR_SEQ_COLUMN_FAMILY_BYTES = BUCKET_DIR_SEQ_COLUMN_FAMILY.getBytes();
    public final static String BUCKET_DIR_SEQ_COLUMN_QUALIFIER = "seq";
    public final static byte[] BUCKET_DIR_SEQ_COLUMN_QUALIFIER_BYTES = BUCKET_DIR_SEQ_COLUMN_QUALIFIER.getBytes();


    public static String getDirTableName(String bucketName) {
        return DIR_TABLE_PREFIX + bucketName;
    }

    public static String getObjTableName(String bucketName) {
        return OBJ_TABLE_PREFIX + bucketName;
    }

    public static String[] getDirColumnFamilies() {
        return new String[]{DIR_META_COLUMN_FAMILY, DIR_SUB_COLUMN_FAMILY};
    }

    public static String[] getObjColumnFamilies() {
        return new String[]{OBJ_META_COLUMN_FAMILY, OBJ_CONTENT_COLUMN_FAMILY};
    }

    //注册用户失败,用户名已存在
    public static final Integer REGISTER_USER_FAIL = 101;
    //hbase创建表失败
    public static final Integer CREATE_HBASE_FAIL = 102;
    //hbase删除表失败
    public static final Integer REMOVE_HBASE_FAIL = 103;
    //hbase删除列族失败
    public static final Integer DELETE_HBASE_COLUMN_FAMILY_FAIL = 104;
    //hbase删除列标识失败
    public static final Integer DELETE_HBASE_COLUMN_QUALIFIER_FAIL = 105;
    //hbase删除行失败
    public static final Integer DELETE_HBASE_ROW_FAIL = 106;
    //hbase内部错误
    public static final Integer CONNECT_HBASE_ERROR = 107;
    //hbase删除列失敗
    public static final Integer GET_HBASE_ROW_DATA_FAIL = 108;
    //hbase添加列數據
    public static final Integer PUT_HBASE_ROW_DATA_FAIL = 109;
    //hbase批量添加行失败
    public static final Integer BATCH_PUT_HBASE_ROW_FAIL = 110;
    //保存文件失败(创建目录失败)
    public static final Integer CREATE_HDFS_DIR_FAIL = 111;
    //被删目录不为空
    public static final Integer DIR_NOT_EMPTY = 112;
    //新建文件失败
    public static final Integer CREATE_FILE_FAIL = 113;
    // 新建文件夹：父目录不存在
    public static final Integer PARENT_DIR_NOT_EXIST = 114;
    //文件夹名称不能为空
    public static final Integer NEW_DIR_NAME_CANNOT_BE_NULL = 115;
    // 文件名不能为空
    public static final Integer FILENAME_CAN_NOT_BE_NULL = 116;
    // 文件夹已存在
    public static final Integer DIR_ALREADY_EXIST = 117;
}
