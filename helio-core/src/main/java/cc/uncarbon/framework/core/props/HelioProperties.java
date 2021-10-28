package cc.uncarbon.framework.core.props;

import cc.uncarbon.framework.core.enums.IdGeneratorStrategyEnum;
import cc.uncarbon.framework.core.enums.TenantIsolateLevelEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Uncarbon
 * 读取配置文件里的值
 */

@ConfigurationProperties(prefix = "helio", ignoreInvalidFields = true)
@Data
public class HelioProperties {

    private final Security security = new Security();
    private final Crud crud = new Crud();
    private final Knife4j knife4j = new Knife4j();


    @Data
    public static class Security {

        /**
         * 放行路由地址(不进行登录校验)
         */
        @ApiModelProperty(value = "放行路由地址(不进行登录校验)")
        private final List<String> excludeRoutes = new ArrayList<>(64);

    }

    @Data
    public static class Crud {

        /**
         * 多租户插件
         */
        private final Tenant tenant = new Tenant();

        /**
         * 乐观锁插件
         */
        private final OptimisticLock optimisticLock = new OptimisticLock();

        /**
         * ID生成器
         */
        private final IdGenerator idGenerator = new IdGenerator();

        /**
         * 用的哪种数据库 如: mysql  postgresql
         * 参考com.baomidou.mybatisplus.annotation.DbType
         * 默认为mysql
         */
        @ApiModelProperty(value = "用的哪种数据库", notes = "如: mysql postgresql(参考com.baomidou.mybatisplus.annotation.DbType)(默认为mysql)")
        private String dbType = "mysql";


        @Data
        public static class Tenant {

            /**
             * 是否启用多租户
             * 默认为false
             */
            @ApiModelProperty(value = "是否启用多租户", notes = "默认为false")
            private Boolean enabled = false;

            /**
             * 多租户隔离级别
             * 默认为行级
             */
            @ApiModelProperty(value = "多租户隔离级别", notes = "默认为行级")
            private TenantIsolateLevelEnum isolateLevel = TenantIsolateLevelEnum.LINE;

            /**
             * 哪些表不启用租户隔离
             */
            @ApiModelProperty(value = "哪些表不启用租户隔离")
            private List<String> ignoredTables = new ArrayList<>(64);

        }

        @Data
        public static class OptimisticLock {

            /**
             * 是否启用乐观锁
             * 默认为false
             */
            @ApiModelProperty(value = "是否启用乐观锁", notes = "默认为false")
            private Boolean enabled = false;

        }

        @Data
        public static class IdGenerator {

            /**
             * ID生成器策略
             * 默认为SNOWFLAKE
             */
            @ApiModelProperty(value = "ID生成器策略", notes = "默认为SNOWFLAKE")
            private IdGeneratorStrategyEnum strategy = IdGeneratorStrategyEnum.SNOWFLAKE;

            /**
             * 雪花ID-数据中心ID
             * 默认为0
             */
            @ApiModelProperty(value = "雪花ID-数据中心ID", notes = "默认为0")
            private Long datacenterId = 0L;

            /**
             * 雪花ID-起始时刻
             * 默认为2021-01-01
             */
            @ApiModelProperty(value = "雪花ID-起始时刻", notes = "默认为2021-01-01")
            String epochDate = "2021-01-01";

        }
    }

    @Data
    public static class Knife4j {

        /**
         * 标题
         */
        @ApiModelProperty(value = "标题")
        private String title = "";

        /**
         * 简介
         */
        @ApiModelProperty(value = "简介")
        private String description = "";

        /**
         * 版本号
         */
        @ApiModelProperty(value = "版本号")
        private String version = "";

    }
}
