package cn.tuzuku.canal.mysqlredis.core;

import com.alibaba.otter.canal.protocol.CanalEntry;

public interface ModelMappingHandler<T> {

    void insert(String database, String table, T data);

    void update(String database, String table, T data);

    void delete(String database, String table, T data);


    default void execute(String database, String table, T data, CanalEntry.EventType type) {
        switch (type) {
            case INSERT:
                insert(database, table, data);
                break;
            case UPDATE:
                update(database, table, data);
                break;
            case DELETE:
                delete(database, table, data);
                break;
            default:
                break;
        }
    }


}
