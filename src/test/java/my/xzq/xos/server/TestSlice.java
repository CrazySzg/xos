package my.xzq.xos.server;

import my.xzq.xos.server.dto.request.MD5ListParam;
import my.xzq.xos.server.utils.JsonUtil;
import my.xzq.xos.server.utils.OkHttpUtil;
import okhttp3.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSlice {

    @Test
    public void test() throws Exception {
        File file = new File("/home/xuezhiqiang/Pictures/Balloon_by_Matt_Benson.jpg");
        FileInputStream inputStream = new FileInputStream(file);
        System.out.println(DigestUtils.md5DigestAsHex(inputStream));
    }


    @Test
    public void test2() throws Exception {
        File file = new File("/home/xuezhiqiang/Pictures/Balloon_by_Matt_Benson.jpg");
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        byte[] bytes = new byte[1];
        randomAccessFile.read(bytes);
        System.out.println(bytes);
        randomAccessFile.read(bytes);
        System.out.println(bytes);
        randomAccessFile.seek(1);
        randomAccessFile.read(bytes);
        System.out.println();
    }

    @Test
    public void test3() throws Exception {
        List<String> list = new ArrayList<>();
        int chunkSize = 1024;
        File file = new File("/home/xuezhiqiang/Downloads/QQPlayer_Setup_39_936.exe");
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[4194304];
        inputStream.read(bytes);
        list.add(DigestUtils.md5DigestAsHex(bytes));
        inputStream.read(bytes);
        list.add(DigestUtils.md5DigestAsHex(bytes));
        System.out.println(JsonUtil.toJson(list));
        System.out.println(System.currentTimeMillis());
    }

    @Test
    public void test4() throws Exception {
        List<String> list = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        int chunkSize = 4 * 1024;
        byte[] bytes;
        String url = "http://127.0.0.1:8080/xos/main/preCreate";
        File file = new File("/home/xuezhiqiang/Pictures/Balloon_by_Matt_Benson.jpg");
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long fileLength = raf.length();
        long count = 0;
        while(count != fileLength) {
            if(count + chunkSize >= fileLength) {
                bytes = new byte[(int)(fileLength - count)];
                count = fileLength;
            } else {
                bytes = new byte[chunkSize];
                count += chunkSize;
            }
            raf.read(bytes);
            String md5 = DigestUtils.md5DigestAsHex(bytes);
            list.add(md5);
        }

        MD5ListParam param = new MD5ListParam();
//        //param.setUploadId("");
//        param.setFileName("Balloon_by_Matt_Benson.jpg");
//        param.setCheckMd5(list);
//        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
//        Request request = new Request.Builder()
//                .header("Authorization","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4enEiLCJqdGkiOiJ4enEiLCJpYXQiOjE1NTI2Mzc1NTYsImV4cCI6MTU1MjY0MTE1Nn0.TmWDvnwfomWJ3trrlNNsgAtrujD1qdbLpKYPao_qfDs")
//                .url(url)
//                .post(RequestBody.create(mediaType, JsonUtil.toJson(param,true)))
//                .build();
//        Response response = client.newCall(request).execute();


        System.out.println(JsonUtil.toJson(list,true));
        System.out.println(list.size());
    //    System.out.println(response.body());


    }

    @Test
    public void test5() throws Exception {
        List<String> list = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        int chunkSize = 4 * 1024 * 1024;
        byte[] bytes;
        String url = "http://127.0.0.1:8088/xos/main/create-object";
        String fileName = "Balloon_by_Matt_Benson.jpg";
        File file = new File("/home/xuezhiqiang/Pictures/Balloon_by_Matt_Benson.jpg");
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long fileLength = file.length();

        Map<String,String> header = new HashMap<>();
        header.put("Authorization","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4enEiLCJqdGkiOiJ4enEiLCJpYXQiOjE1NTM1OTMwMDYsImV4cCI6MTU1MzU5NjYwNn0.lIcVNpUsnXghvk4sEH0CA2KtNXAuSugc7mQ2PEbHy_k");

        Map<String,String> nameValue = new HashMap<>();
        nameValue.put("fileSize",String.valueOf(fileLength));
        nameValue.put("fileName",fileName);
        nameValue.put("targetDir","/");
        nameValue.put("category","1");
        nameValue.put("suffix",",jpg");
   //     nameValue.put("chunkMD5",md5);
        nameValue.put("uploadId","5528c0a8a2814de6adbf41023d5340c3");
   //     nameValue.put("partSeq","0");

        long count = 0;
        long index = 0;
        while(count != fileLength) {

            if(count + chunkSize >= fileLength) {
                bytes = new byte[(int)(fileLength - count)];

                count = fileLength;
            } else {
                bytes = new byte[chunkSize];

                count += chunkSize;
            }
            raf.read(bytes);
            String md5 = DigestUtils.md5DigestAsHex(bytes);
            nameValue.put("chunkMD5",md5);
            nameValue.put("partSeq",String.valueOf(index));
            OkHttpUtil.upload(url, bytes, fileName, header, nameValue);
            index++;
            System.out.println(index);

        }
    }
}
