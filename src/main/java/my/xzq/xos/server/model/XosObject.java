package my.xzq.xos.server.model;

import lombok.Data;
import okhttp3.Response;

import java.io.InputStream;


@Data
public class XosObject {

    private ObjectMetaData metaData;
    private InputStream content;
    private Response response;

    public XosObject() {
    }

    public XosObject(Response response) {
        this.response = response;
    }

    // 释放资源
    public void close() {
        try {
            if (this.content != null) {
                this.content.close();
            }
            if (this.response != null) {
                this.response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
