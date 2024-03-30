package cn.tuzuku.canal.mysqlredis.core;

import com.google.gson.Gson;

public class GsonUtils {

    private static final Gson GSON = new Gson();

    private GsonUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

}
