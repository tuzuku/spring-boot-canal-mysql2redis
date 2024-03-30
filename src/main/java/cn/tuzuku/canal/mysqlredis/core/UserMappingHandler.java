package cn.tuzuku.canal.mysqlredis.core;

import cn.tuzuku.canal.mysqlredis.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UserMappingHandler implements ModelMappingHandler<User> {
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String PREDIX = "user:";

    public UserMappingHandler(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void insert(String database, String table, User data) {
        log.info("执行插入操作");
        redisTemplate.opsForValue().set(PREDIX + data.getId(), data);
    }

    @Override
    public void update(String database, String table, User data) {
        log.info("执行更新操作操作");
        redisTemplate.opsForValue().setIfPresent(PREDIX + data.getId(), data);
    }

    @Override
    public void delete(String database, String table, User data) {
        Object o = redisTemplate.opsForValue().get(PREDIX + data.getId());
        if (o != null) {
            log.info("执行删除操作");
            redisTemplate.delete(PREDIX + data.getId());
            return;
        }
        log.info("删除的数据不存在");
    }
}
