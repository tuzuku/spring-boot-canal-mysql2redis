package cn.tuzuku.canal.mysqlredis.core;

import cn.tuzuku.canal.mysqlredis.model.Model;
import cn.tuzuku.canal.mysqlredis.model.User;

import java.util.HashMap;
import java.util.Map;

public class ModelConfig {

    public static final Map<String, Class<? extends Model>> MODEL_MAP = new HashMap<>();

    static {
        MODEL_MAP.put("test.user", User.class);
    }
}
