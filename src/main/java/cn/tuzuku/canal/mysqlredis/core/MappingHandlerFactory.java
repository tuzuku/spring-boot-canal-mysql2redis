package cn.tuzuku.canal.mysqlredis.core;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MappingHandlerFactory implements ApplicationContextAware {

    private Map<String, ModelMappingHandler> modelMappingHandlerMap = new java.util.HashMap<>();


    public ModelMappingHandler getMappingHandler(String tableName) {
        return modelMappingHandlerMap.get(tableName);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, ModelMappingHandler> beansOfType = applicationContext.getBeansOfType(ModelMappingHandler.class);
        beansOfType.forEach((k, v) -> modelMappingHandlerMap.put(v.getTableName(), v));

    }
}
