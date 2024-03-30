package cn.tuzuku.canal.mysqlredis.core;

import cn.tuzuku.canal.mysqlredis.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UserMappingHandler implements ModelMappingHandler<User> {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TABLE_NAME = "test.user";
    private static final String PREFIX = "user:";

    public UserMappingHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void insert(String database, String table, User data) {
        log.info("执行插入操作");
        String key = PREFIX + data.getId();
        redisTemplate.opsForValue().set(key, GsonUtils.toJson(data));
    }

    @Override
    public void update(String database, String table, User data) {
        log.info("执行更新操作操作");
        redisTemplate.opsForValue().setIfPresent(PREFIX + data.getId(), GsonUtils.toJson(data));
    }

    @Override
    public void delete(String database, String table, User data) {
        String key = PREFIX + data.getId();
        Object o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            log.info("执行删除操作");
            redisTemplate.delete(key);
            return;
        }
        log.info("删除的数据不存在");
    }
}
