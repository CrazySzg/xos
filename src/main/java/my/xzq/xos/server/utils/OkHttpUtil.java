package my.xzq.xos.server.utils;

import okhttp3.*;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class OkHttpUtil {

    private static OkHttpClient client = new OkHttpClient();

    public static ResponseBody upload(String url, byte[] file, String fileName, Map<String,String> header,Map<String,String> nameValue) throws IOException {

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("multipart/form-data"), file));

        for(Map.Entry<String,String> entry : nameValue.entrySet()) {
            builder.addFormDataPart(entry.getKey(),entry.getValue());
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .headers(Headers.of(header))
                .url(url)
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();

        return response.body();

    }


    public static ResponseBody postRequest(String url,String json) throws Exception {
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, json))
                .build();
        Response response = client.newCall(request).execute();
        return response.body();
    }
}
