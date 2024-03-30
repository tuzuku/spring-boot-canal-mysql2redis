package cn.tuzuku.canal.mysqlredis;

import cn.tuzuku.canal.mysqlredis.core.SimpleCanalClient;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CanalMysqlRedisApplication {
    private final SimpleCanalClient simpleCanalClient;

    public CanalMysqlRedisApplication(SimpleCanalClient simpleCanalClient) {
        this.simpleCanalClient = simpleCanalClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(CanalMysqlRedisApplication.class, args);
    }

    @PostConstruct
    public void init() {
        simpleCanalClient.execute();
    }
}
