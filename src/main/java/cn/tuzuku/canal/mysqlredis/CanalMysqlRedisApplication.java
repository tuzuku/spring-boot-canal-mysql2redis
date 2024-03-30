package cn.tuzuku.canal.mysqlredis;

import cn.tuzuku.canal.mysqlredis.core.SimpleCanalClient;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.SpringServletContainerInitializer;

import java.util.Set;

@SpringBootApplication
public class CanalMysqlRedisApplication extends SpringServletContainerInitializer {
    private final SimpleCanalClient simpleCanalClient;

    public CanalMysqlRedisApplication(SimpleCanalClient simpleCanalClient) {
        this.simpleCanalClient = simpleCanalClient;
    }


    @Override
    public void onStartup(Set<Class<?>> webAppInitializerClasses, ServletContext servletContext) throws ServletException {
        super.onStartup(webAppInitializerClasses, servletContext);
    }

    public static void main(String[] args) {
        SpringApplication.run(CanalMysqlRedisApplication.class, args);
    }


    @PostConstruct
    public void init() {
        simpleCanalClient.execute(new String[]{});
    }
}
