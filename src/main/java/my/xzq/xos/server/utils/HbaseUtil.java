package my.xzq.xos.server.utils;

import com.alibaba.fastjson.JSON;
import my.xzq.xos.server.common.XosConstant;
import my.xzq.xos.server.exception.XosException;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2019-03-11 10:37
 */
@Component
public class HbaseUtil {

    @Autowired
    @Qualifier("hbaseConnection")
    private Connection connection;


    /**
     * 创建表
     *
     * @param tableName
     * @param columnFamilies
     * @return
     */
    public boolean createTable(String tableName, String[] columnFamilies) {
        try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
            HTableDescriptor table = new HTableDescriptor(TableName.valueOf(tableName));
            if (admin.tableExists(tableName)) {
                //表已经存在
                return false;
            }
            Arrays.stream(columnFamilies).forEach(e -> {
                HColumnDescriptor columnFamily = new HColumnDescriptor(e);
                columnFamily.setMaxVersions(1);
                table.addFamily(columnFamily);
            });

            //创建表
            admin.createTable(table);

        } catch (IOException e) {
            e.printStackTrace();
            throw new XosException(XosConstant.CREATE_HBASE_FAIL, "Create table " + tableName + " fail");
        }
        return true;
    }

    /**
     * 删除表
     *
     * @param tableName
     * @return
     */
    public boolean removeTable(String tableName) {
        try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new XosException(XosConstant.REMOVE_HBASE_FAIL, "remove table " + tableName + " fail");
        }
        return true;
    }

    public boolean deleteColumnFamily(String tableName, String columnFamily) {
        try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
            admin.deleteColumn(tableName, columnFamily);
        } catch (IOException e) {
            e.printStackTrace();
            throw new XosException(XosConstant.DELETE_HBASE_COLUMN_FAMILY_FAIL, "delete columnFamily:" + columnFamily + " of tableName:" + tableName + " fail");
        }
        return true;
    }

    public boolean deleteColumnQualifier(String tableName, String rowKey, String columnFamily, String columnQualifier) {
        try {
            Delete delete = new Delete(rowKey.getBytes());
            //删除指定列的最新版本
            delete.addColumn(columnFamily.getBytes(), columnQualifier.getBytes());
            return deleteRow(tableName, delete);
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.DELETE_HBASE_COLUMN_QUALIFIER_FAIL, "delete rowkey:columnFamily:columnQualifier" + rowKey + ":" + columnFamily +
                    ":" + columnQualifier + " of tableName:" + tableName + " fail");
        }
    }

    public boolean deleteRow(String tableName, Delete delete) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {

            table.delete(delete);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            throw new XosException(XosConstant.DELETE_HBASE_ROW_FAIL, "delete row:" + Bytes.toString(delete.getRow()) + " fail");
        }
    }

    public boolean deleteRows(String tableName, List<String> rowKeys) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            if (CollectionUtils.notEmpty(rowKeys)) {
                List<Delete> deleteList = rowKeys.stream()
                        .map(e -> new Delete(Bytes.toBytes(e)))
                        .collect(Collectors.toList());
                table.delete(deleteList);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new XosException(XosConstant.DELETE_HBASE_ROW_FAIL, "delete rows:" + JSON.toJSONString(rowKeys) + " fail");
        }
        return false;
    }

    public boolean existRow(String tableName, String rowKey) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(rowKey.getBytes());
            return table.exists(get);
        } catch (IOException e) {
            e.printStackTrace();
            String msg = String.format("check exists row from table=%s error. msg=%s", tableName, e.getMessage());
            throw new XosException(XosConstant.CONNECT_HBASE_ERROR, msg);
        }
    }

    public Result getRow(String tableName, String rowKey) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(rowKey.getBytes());
            return table.get(get);
        } catch (IOException e) {
            e.printStackTrace();
            String msg = String.format("get row from table=%s error. msg=%s", tableName, e.getMessage());
            throw new XosException(XosConstant.GET_HBASE_ROW_DATA_FAIL, msg);
        }
    }

    public Result getFilteredRow(String tableName, String rowKey, FilterList filterList) {
        Result rs;
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(Bytes.toBytes(rowKey));
            get.setFilter(filterList);
            rs = table.get(get);
        } catch (IOException e) {
            e.printStackTrace();
            String msg = String.format("get row from table=%s error. msg=%s", tableName, e.getMessage());
            throw new XosException(XosConstant.GET_HBASE_ROW_DATA_FAIL, msg);
        }
        return rs;
    }


    public Result getRow(String tableName, String row, byte[] family, byte[] qualifier) {
        Result rs;
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(Bytes.toBytes(row));
            get.addColumn(family, qualifier);
            rs = table.get(get);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = String
                    .format("get row from table=%s error. msg=%s", tableName, e.getMessage());
            throw new XosException(XosConstant.GET_HBASE_ROW_DATA_FAIL, msg);
        }
        return rs;
    }

    public Result[] getRows(String tableName, List<String> rowKeys, FilterList filterList) {
        Result[] rs = null;
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            if (CollectionUtils.notEmpty(rowKeys)) {
                List<Get> getList = rowKeys.stream()
                        .filter(e -> e != null)
                        .map(e -> {
                            Get get = new Get(e.getBytes());
                            get.setFilter(filterList);
                            return get;
                        }).collect(Collectors.toList());
                return table.get(getList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = String
                    .format("get rows %s from table=%s error. msg=%s", JSON.toJSONString(rowKeys), tableName, e.getMessage());
            throw new XosException(XosConstant.GET_HBASE_ROW_DATA_FAIL, msg);
        }
        return rs;
    }


    public Result[] getRows(String tableName, List<String> rowKeys) {
        Result[] rs = null;
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            if (CollectionUtils.notEmpty(rowKeys)) {
                List<Get> getList = rowKeys.stream()
                        .filter(e -> e != null)
                        .map(e -> {
                            Get get = new Get(e.getBytes());
                            return get;
                        }).collect(Collectors.toList());
                return table.get(getList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = String
                    .format("get rows %s from table=%s error. msg=%s", JSON.toJSONString(rowKeys), tableName, e.getMessage());
            throw new XosException(XosConstant.GET_HBASE_ROW_DATA_FAIL, msg);
        }
        return rs;
    }

    public ResultScanner getScanner(String tableName, Scan scan) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            return table.getScanner(scan);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = String
                    .format("get rows from table=%s error. msg=%s", tableName, e.getMessage());
            throw new XosException(XosConstant.GET_HBASE_ROW_DATA_FAIL, msg);
        }
    }


    public ResultScanner getScanner(String tableName, String rowKey, String endKey, FilterList filterList) {
        Scan scan = new Scan();
        scan.withStartRow(Bytes.toBytes(rowKey));
        scan.withStopRow(Bytes.toBytes(endKey));
        scan.setFilter(filterList);
        scan.setCaching(1000);
        return getScanner(tableName,scan);
    }

    public ResultScanner getScanner(String tableName,FilterList filterList) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Scan scan = new Scan();
            scan.setFilter(filterList);
            scan.setCaching(1000);
            return table.getScanner(scan);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = String
                    .format("get row from table=%s error. msg=%s",  tableName, e.getMessage());
            throw new XosException(XosConstant.GET_HBASE_ROW_DATA_FAIL, msg);
        }
    }

    public ResultScanner getScanner(String tableName) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Scan scan = new Scan();
            scan.setCaching(1000);
            return table.getScanner(scan);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = String
                    .format("get row from table=%s error. msg=%s",  tableName, e.getMessage());
            throw new XosException(XosConstant.GET_HBASE_ROW_DATA_FAIL, msg);
        }
    }

    public boolean putRow(String tableName, Put put) {
        try(Table table = connection.getTable(TableName.valueOf(tableName))) {
            table.put(put);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.PUT_HBASE_ROW_DATA_FAIL, "put row error");
        }
    }

    public boolean putRow(String tableName, String rowKey, String columnFamily, String columnQuanlifier, String data) {
        Put put = new Put(rowKey.getBytes());
        put.addColumn(columnFamily.getBytes(), columnQuanlifier.getBytes(), data.getBytes());
        return putRow(tableName, put);
    }

    public long incrementColumnValue(String tableName, String row, byte[] columnFamily, byte[] columnQuanlifier, int number) {
        try(Table table = connection.getTable(TableName.valueOf(tableName))) {
            return table.incrementColumnValue(row.getBytes(), columnFamily, columnQuanlifier, number);
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.PUT_HBASE_ROW_DATA_FAIL, "put row error");
        }
    }

    /**
     * 批量插入行数据
     * @param connection
     * @param tableName
     * @param puts
     * @return
     */
    public static boolean batchPutRows(Connection connection, String tableName, List<Put> puts) {
        final BufferedMutator.ExceptionListener listener = new BufferedMutator.ExceptionListener() {
            @Override
            public void onException(RetriesExhaustedWithDetailsException e, BufferedMutator mutator) {
                String msg = String
                        .format("put rows from table=%s error. msg=%s", tableName, e.getMessage());
                throw new XosException(XosConstant.BATCH_PUT_HBASE_ROW_FAIL, msg);
            }
        };
        BufferedMutatorParams params = new BufferedMutatorParams(TableName.valueOf(tableName))
                .listener(listener);
        params.writeBufferSize(5 * 1024 * 1024);
        try (final BufferedMutator mutator = connection.getBufferedMutator(params)) {
            mutator.mutate(puts);
            mutator.flush();
        } catch (Exception e) {
            String msg = String
                    .format("put rows from table=%s error. msg=%s", tableName, e.getMessage());
            throw new XosException(XosConstant.BATCH_PUT_HBASE_ROW_FAIL, msg);
        }
        return true;
    }


}
