package cn.tuzuku.canal.mysqlredis.model;

import cn.tuzuku.canal.mysqlredis.core.Table;
import cn.tuzuku.canal.mysqlredis.core.TableField;
import lombok.Data;

import java.io.Serializable;

@Table(schema = "test", table = "user")
@Data
public class User implements Serializable, Model {


    @TableField("id")
    private Long id;

    @TableField("name")
    private String name;

    @TableField("nick_name")
    private String nickName;

    @TableField("age")
    private Integer age;


}
