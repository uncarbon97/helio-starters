package cc.uncarbon.framework.helium.db.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 数据源属性配置
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DataSourceSetting {

    /**
     * 数据源别名
     */
    private String alias;

    /**
     * 数据库驱动类名称
     */
    private String driverClassName;

    /**
     * 数据库连接地址
     */
    private String url;

    /**
     * 数据库账号
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

}
