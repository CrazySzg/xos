package my.xzq.xos.server.common;

import org.apache.hadoop.hbase.filter.*;

public final class XosConstant {

    public static final Integer BAD_PARAM = -100000;

    public static final Integer SUCCESS = 200;

    public static final String SEPARATOR = "/";

    public static final String ROOT = "0-";

    public static final String STUB = "-";

    public static final String DOWNLOAD_TOKEN_SPLITER = "###";

    public static final Integer CHUNK_SIZE = 4 * 1024;

    public static final String BUCKET = "bucket";

    public static final String FILEPATH = "filePath";

    // 目录表前缀
    public final static String DIR_TABLE_PREFIX = "xos_dir_";
    // 对象表前缀
    public final static String OBJ_TABLE_PREFIX = "xos_obj_";

    public final static Integer UPLOAD_TASK_FINISH = 1;

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
    // 目录的父级
    public final static byte[] DIR_PARENT_QUALIFIER = "parent".getBytes();
    // 下载需要的盐值
    public final static byte[] OBJ_DOWNLOAD_SALT_QUALIFIER = "salt".getBytes();
    // 当前文件所在真实目录
    public final static byte[] OBJ_CURRENT_DIR_QUALIFIER = "current_dir".getBytes();
    // 文件名用于搜索
    public final static byte[] OBJ_FILENAME_QUALIFIER = "fileName".getBytes();
    // 文件表content列名
    public final static byte[] OBJ_CONTENT_QUALIFIER = "content".getBytes();
    // 文件表length列名
    public final static byte[] OBJ_SIZE_QUALIFIER = "size".getBytes();
    // 文件表property列名
    public final static byte[] OBJ_STATUS_QUALIFIER = "status".getBytes();
    // 文件表category列名
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
    // 剩余空间不足
    public static final Integer NO_EXTRA_SPACE_LEAVE = 118;
    // 上传文件失败
    public static final Integer UPLOAD_FAIL = 119;
    //新建文件夹失败
    public static final Integer MAKE_DIR_FAIL = 120;
    //删除操作失败
    public static final Integer DELETE_OPER_FAIL = 121;
    //获取列表失败
    public static final Integer LIST_DIR_FAIL = 122;
    // 文件下载失败
    public static final Integer DOWNLOAD_FAIL = 123;
    // 输入两次密码不一致
    public static final Integer PASSWORD_NOT_EQUAL = 124;
    // 该邮箱已被注册
    public static final Integer EMAIL_ALREADY_BINDED = 125;
    // 重命名失败
    public static final Integer RENAME_FAIL = 126;
    // 文件名不能空
    public static final Integer NEW_NAME_CAN_NOT_NULL = 127;
    // 文件名不能重复
    public static final Integer FILENAME_CAN_NOT_REPEAT = 128;
    // 根目录无法删除
    public static final Integer ROOT_DIR_CANNOT_DELETE = 129;
    // 目录不存在
    public static final Integer DIR_NOT_EXIST = 130;
    // 请求非法
    public static final Integer OPERATION_ILLEGAL = 131;
    // 上传目录发生冲突
    public static final Integer DIR_COLLISION = 132;
    // 检索失败
    public static final Integer SEARCH_FAIL = 133;
    // 移动文件失败
    public static final Integer MOVE_FAIL = 134;
    // 不能识别的类别
    public static final Integer UNRECOGNIZE_CATEGORY = 135;
}
