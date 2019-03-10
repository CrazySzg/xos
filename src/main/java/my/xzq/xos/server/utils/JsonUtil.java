package my.xzq.xos.server.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static ObjectMapper objMapper = new ObjectMapper();

    static {
        objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        objMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
                false);
    }

    public static String toJson(Object obj) throws IOException {
        return objMapper.writeValueAsString(obj);
    }


    public static String toJson(Object obj, boolean prettyFormat) throws IOException {
        String json = objMapper.writeValueAsString(obj);
        if (prettyFormat) {
            Object jsonObj = objMapper.readValue(json, Object.class);
            json = objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
        }
        return json;
    }


    public static <T> T fromJson(String json, Class<T> clazz)
            throws IOException {
        return objMapper.readValue(json, clazz);
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz)
            throws IOException {
        return objMapper.readValue(json, objMapper.getTypeFactory()
                .constructCollectionType(List.class, clazz));
    }

    public static <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyClazz, Class<V> valueClazz)
            throws IOException {
        return objMapper.readValue(json, objMapper.getTypeFactory()
                .constructMapType(HashMap.class, keyClazz, valueClazz));
    }

}
