package cc.uncarbon.framework.helio.base.autoconfigure;

import cc.uncarbon.framework.helio.base.constant.HelioConstant;
import cc.uncarbon.framework.helio.base.enums.TenantStrategyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 配置属性会被反序列化到本 Spring Bean 中，可自行依赖注入后使用
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = "helio")
@Data
public class HelioProperties {

    private final Satoken satoken = new Satoken();

    private final WebSecurity webSecurity = new WebSecurity();
    private final WebLogging webLogging = new WebLogging();
    private final Mybatis mybatis = new Mybatis();
    private final Tenant tenant = new Tenant();
    private final I18n i18n = new I18n();


    @Data
    public static class Satoken {

        private final LocalCacheDao localCacheDao = new LocalCacheDao();

        /**
         * 在 SA-Token 读取时，本地缓存一定时长，减少对 Redis 的 IO
         * 在多实例部署时存在一定安全风险：用户登出时，A机器的缓存已清除，但B机器的缓存还认为有效
         * 所以请谨慎设置本地缓存有效时长
         *
         * @since 2.1.0
         */
        @Data
        public static class LocalCacheDao {

            /**
             * 是否启用
             */
            private Boolean enabled = false;

            /**
             * 本地缓存有效时长，单位=秒
             */
            private long duration = 5;

            /**
             * 本地缓存最大容量，注意：每次超过容量都会触发一次清理过程
             */
            private int capacity = 4000;

        }
    }


    @Data
    public static class WebSecurity {

        private final Xss xss = new Xss();


        @Data
        public static class Xss {

            /**
             * 是否启用 XSS Filter
             * 默认启用
             */
            private boolean enabled = true;

            /**
             * 不进行XSS过滤的路由，直接放行
             */
            private List<String> ignoredUrls = new ArrayList<>();

        }
    }

    @Data
    public static class WebLogging {

        /**
         * 是否启用 Web 访问日志切面，默认为 false
         */
        private Boolean enabled = Boolean.FALSE;

    }

    @Data
    public static class Mybatis {

        /**
         * 乐观锁插件
         */
        private final OptimisticLock optimisticLock = new OptimisticLock();

        /**
         * ID生成器
         */
        private final IdGenerator idGenerator = new IdGenerator();

        /**
         * 用的哪种数据库 如: mysql  postgresql 参考com.baomidou.mybatisplus.annotation.DbType 默认为mysql
         */
        private String dbType = "mysql";


        @Data
        public static class OptimisticLock {

            /**
             * 是否启用乐观锁 默认为 true
             */
            private Boolean enabled = true;

        }

        @Data
        public static class IdGenerator {

            /**
             * 雪花ID-起始时刻 默认为2021-01-01
             */
            String epochDate = "2021-01-01";

            /**
             * 雪花ID-数据中心ID 默认为0
             */
            private Long datacenterId = 0L;

        }
    }

    @Data
    public static class Tenant {

        /**
         * 是否启用多租户 默认为 false
         */
        private Boolean enabled = false;

        /**
         * 多租户隔离级别 默认为行级
         */
        private TenantStrategyEnum isolateLevel = TenantStrategyEnum.LINE;

        /**
         * 哪些表忽略租户隔离 仅对【行级】隔离级别有效
         */
        private Collection<String> ignoredTables = new LinkedHashSet<>(64);

        /**
         * 特权租户ID，该租户可以无视租户SQL拦截器 目前仅对【行级】隔离级别有效 默认为 0
         */
        private Long privilegedTenantId = HelioConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID;

    }

    @Data
    public static class I18n {

        /**
         * 是否启用国际化 默认为 false
         */
        private Boolean enabled = Boolean.FALSE;

    }
}
