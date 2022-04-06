package cc.uncarbon.framework.crud.dynamicdatasource;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DataSourceDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据源名称，切换数据源时需要这个，最好不用数字开头
     */
    @ApiModelProperty(value = "数据源名称", notes = "切换数据源时需要这个，最好不用数字开头")
    private String name;

    /**
     * 数据库驱动类名称
     */
    @ApiModelProperty(value = "数据库驱动类名称")
    private String driverClassName;

    /**
     * 数据库连接地址
     */
    @ApiModelProperty(value = "数据库连接地址")
    private String url;

    /**
     * 数据库账号
     */
    @ApiModelProperty(value = "数据库账号")
    private String username;

    /**
     * 数据库密码
     */
    @ApiModelProperty(value = "数据库密码")
    private String password;

}
