package cc.uncarbon.framework.core.props;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.enums.IdGeneratorStrategyEnum;
import cc.uncarbon.framework.core.enums.TenantIsolateLevelEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置项会被反序列化到本 Spring Bean 中
 * 可自行依赖注入后使用
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = "helio")
@Data
public class HelioProperties {

    private final Security security = new Security();
    private final Crud crud = new Crud();
    private final Knife4j knife4j = new Knife4j();
    private final Tenant tenant = new Tenant();
    private final WebLogging webLogging = new WebLogging();


    @Data
    public static class Security {

        /**
         * 放行路由地址，不进行登录校验
         */
        private final List<String> excludeRoutes = new ArrayList<>(64);

    }

    @Data
    public static class Crud {

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
        private String dbType = "mysql";


        @Data
        public static class OptimisticLock {

            /**
             * 是否启用乐观锁
             * 默认为 true
             */
            private Boolean enabled = true;

        }

        @Data
        public static class IdGenerator {

            /**
             * ID生成器策略
             * 默认为SNOWFLAKE
             */
            private IdGeneratorStrategyEnum strategy = IdGeneratorStrategyEnum.SNOWFLAKE;

            /**
             * 雪花ID-起始时刻
             * 默认为2021-01-01
             */
            String epochDate = "2021-01-01";

            /**
             * 雪花ID-数据中心ID
             * 默认为0
             */
            private Long datacenterId = 0L;

        }
    }

    @Data
    public static class Knife4j {

        /**
         * Knife4j UI 上显示的标题
         */
        private String title = "";

        /**
         * Knife4j UI 上显示的简介
         */
        private String description = "";

        /**
         * Knife4j UI 上显示的版本号
         */
        private String version = "";

    }

    @Data
    public static class Tenant {

        /**
         * 是否启用多租户
         * 默认为 false
         */
        private Boolean enabled = false;

        /**
         * 多租户隔离级别
         * 默认为行级
         */
        private TenantIsolateLevelEnum isolateLevel = TenantIsolateLevelEnum.LINE;

        /**
         * 哪些表忽略租户隔离
         * 仅对【行级、表级】隔离级别有效
         */
        private Collection<String> ignoredTables = new LinkedHashSet<>(64);

        /**
         * 特权租户ID，该租户可以无视租户SQL拦截器
         * 目前仅对【行级】隔离级别有效
         * 默认为 0
         */
        private Long privilegedTenantId = HelioConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID;

    }

    @Data
    public static class WebLogging {

        /**
         * 是否启用 Web 请求记录
         * 默认为 false
         */
        private Boolean enabledRequestLog = false;

    }
}
