package cn.tuzuku.canal.mysqlredis.core;

import cn.tuzuku.canal.mysqlredis.model.User;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleCanalClient {

    private final MappingHandlerFactory modelMappingHandler;


    public void execute() {
        CanalConnector connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress("119.23.61.105", 11111), "test", "canal", "canal");

        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            int totalEmptyCount = 100_000_000;
            while (emptyCount < totalEmptyCount) {
                Message message = connector.getWithoutAck(batchSize);// 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    emptyCount = 0;
                    log.info("message[batchId={},size={}]", batchId, size);
                    printEntry(message.getEntries());
                }

                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

            log.warn("empty too many times, exit");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connector.disconnect();
        }
    }


    private void printEntry(List<Entry> entrys) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            EventType eventType = rowChage.getEventType();
            log.info(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            String schemaName = entry.getHeader().getSchemaName();
            String tableName = entry.getHeader().getTableName();
            //根据Handler 查找对应的处理器
            Class<User> userClass = User.class;
            Table annotation = userClass.getAnnotation(Table.class);
            if (schemaName.equals(annotation.schema()) && tableName.equals(annotation.table())) {
                User user = new User();
                for (RowData rowData : rowChage.getRowDatasList()) {
                    if (eventType == EventType.DELETE) {
                        setColumn(rowData.getBeforeColumnsList(), user);
                    } else if (eventType == EventType.INSERT) {
                        setColumn(rowData.getAfterColumnsList(), user);
                    } else {
                        log.info("-------&gt; before");
                        setColumn(rowData.getBeforeColumnsList(), user);
                        log.info("-------&gt; after");
                        setColumn(rowData.getAfterColumnsList(), user);
                    }
                }
                log.info(user.toString());

                modelMappingHandler.getMappingHandler(annotation.schema() + "." + annotation.table()).execute(schemaName, tableName, user, eventType);

            }

        }
    }

    private static void printColumn(List<Column> columns) {
        for (Column column : columns) {
            log.info("printColumn " + column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }

    private static void setColumn(List<Column> columns, User user) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Class<? extends User> userClass = user.getClass();
        Field[] fields = userClass.getDeclaredFields();
        Map<String, Field> schemaMap = Arrays.stream(fields).collect(Collectors.toMap(i -> i.getAnnotation(TableField.class).value(), Function.identity()));
        Constructor<? extends User> constructor = userClass.getConstructor();
        User newInstance = constructor.newInstance();
        for (Column column : columns) {
            //字段包含，处理数据
            if (schemaMap.containsKey(column.getName())) {
                Field field = schemaMap.get(column.getName());
                field.setAccessible(true);
                if (column.getIsNull()) {
                    continue;
                }
                if (column.getIsKey() || column.getUpdated()) {
                    try {
                        field.set(newInstance, convertValueToFieldType(field.getType(), column.getValue()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        BeanUtils.copyProperties(newInstance, user);
    }

    private static Object convertValueToFieldType(Class<?> fieldType, Object value) {
        if (value == null) {
            return null;
        }

        // Ensure the value is a String before attempting to parse it
        if (!(value instanceof String)) {
            log.info("Value is not a String: {}", value);
            return null;
        }

        String stringValue = (String) value;

        // If the types are already compatible, return the value as is
        if (fieldType.isAssignableFrom(value.getClass())) {
            return value;
        }

        try {
            if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
                return Integer.parseInt(stringValue);
            } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
                return Long.parseLong(stringValue);
            } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                return Double.parseDouble(stringValue);
            } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                return Float.parseFloat(stringValue);
            } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                return Boolean.parseBoolean(stringValue);
            } else if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
                return Byte.parseByte(stringValue);
            } else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
                return Short.parseShort(stringValue);
            } else if (fieldType.equals(char.class) || fieldType.equals(Character.class)) {
                if (stringValue.length() == 1) {
                    return stringValue.charAt(0);
                } else {
                    log.info("Unable to convert string to char: {}", stringValue);
                    return null;
                }
            }
        } catch (NumberFormatException e) {
            log.info("Unable to convert string '{}' to {}", stringValue, fieldType.getSimpleName());
        }

        // For any other types that are not primitive/wrapper types, return null or implement additional conversion logic
        return null;
    }
}
