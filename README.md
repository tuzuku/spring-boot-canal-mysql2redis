# Canal client with Spring Boot

## 项目说明
本项目是基于Spring Boot框架开发的Canal客户端，旨在监控MySQL数据库的更改，并将这些更改同步到Redis缓存中。项目核心基于Canal官方提供的SimpleClient进行定制开发，以满足基本的数据同步需求。在实际部署中，用户可以根据具体的业务场景对客户端进行进一步的扩展和优化。
## 安装redis

1. 拉取镜像
2. 编写redis 配置文件 [测试可选]

   挂载配置文件
   ```shell 
    docker run --name redis -v /path/to/redis.conf:/usr/local/etc/redis/redis.conf -d redis redis-server
    /usr/local/etc/redis/redis.conf

   ```
3. 使用redis配置文件启动redis 容器

   ```shell
   docker run --name redis -e REDIS_PASSWORD=my-secret-pw -d mysql requirepass my-secret-pw 
   ```

## 安装MySQL

1. 拉镜像
    ```shell
      docker pull mysql:latest
      ```

2. 使用配置文件开启同步复制
    1. 执行镜像

       ```shell
       docker run --name some-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:latest 
       ```
    2. mysql 配置
        ```conf
        [mysqld]
        log-bin=mysql-bin
        binlog-format=ROW
        server_id=1 
        ```
       启动后使用 `SHOW VARIABLES LIKE '%log_bin%';` 可以查看是否开启binlog
    3. 挂载配置文件
       ```shell
       docker run --name some-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -v /path/to/custom_my.cnf:/etc/mysql/conf.d/custom_my.cnf:ro -d mysql:latest
       ```

- 也可以开启同步复制（使用docker 命令-临时）

    ```shell
    docker run --name some-mysql \
    -e MYSQL_ROOT_PASSWORD=my-secret-pw \
    -d mysql:latest \
    --log-bin=mysql-bin \
    --binlog-format=ROW \
    --server-id=1
    ```

# 安装canal

canal 是阿里巴巴开源的数据库同步工具，基于数据库增量日志解析，提供增量数据订阅&消费，目前主要支持了mysql

## install canal server

拉取canal 镜像

```bash
docker pull canal/canal-server:latest
```

获取canal 执行脚本

```shell
# 下载脚本
wget https://raw.githubusercontent.com/alibaba/canal/master/docker/run.sh 

# 构建一个destination name为test的队列
sh run.sh -e canal.auto.scan=false \
		  -e canal.destinations=test \
		  -e canal.instance.master.address=127.0.0.1:3306  \
		  -e canal.instance.dbUsername=canal  \
		  -e canal.instance.dbPassword=canal  \
		  -e canal.instance.connectionCharset=UTF-8 \
		  -e canal.instance.tsdb.enable=true \
		  -e canal.instance.gtidon=false  \
```

## 使用spring boot 创建canal 客户端

- canal 客户端依赖、spring boot web 依赖、redis 服务依赖
- 注：canal 客户端需要引入三个依赖
    ```xml
    <dependency>
        <groupId>com.alibaba.otter</groupId>
        <artifactId>canal.client</artifactId>
        <version>1.1.4</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    ```
- 本案例只使用canal 官网给出的SimpleClient 进行改造编写，实际使用中需要根据业务需求进行扩展


