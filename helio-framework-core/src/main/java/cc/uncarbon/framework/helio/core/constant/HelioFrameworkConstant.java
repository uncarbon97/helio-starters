package cc.uncarbon.framework.helio.core.constant;

/**
 * Helio 脚手架基础常量定义
 *
 * @author Uncarbon
 */
public final class HelioFrameworkConstant {
    private HelioFrameworkConstant() {
    }

    public static final class Dubbo {
        private Dubbo() {
        }

        public static final String ENABLE_VALIDATION = "true";
        public static final int TIMEOUT = 10000;
        public static final int RETRIES = -1;
        public static final int RPC_EXCEPTION_RESPONSE_CODE = 1;
    }

    public static final class Message {
        private Message() {
        }

        public static final String NO_DATA = "NO_DATA";
        public static final String SUCCESS = "SUCCESS";
    }

    public static final class Version {
        private Version() {
        }

        /**
         * HTTP API 版本 v1
         */
        public static final String HTTP_API_VERSION_V1 = "/api/v1";

        /**
         * DUBBO API 版本 v1
         */
        public static final String DUBBO_VERSION_V1 = "1.0.0";
    }

    public static final class EntityField {
        private EntityField() {
        }

        /**
         * 租户ID
         */
        public static final String COLUMN_TENANT_ID = "tenant_id";
        public static final String ENTITY_FIELD_TENANT_ID = "tenantId";

        /**
         * 创建时刻
         */
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String ENTITY_FIELD_CREATED_AT = "createdAt";

        /**
         * 创建者
         */
        public static final String COLUMN_CREATED_BY = "created_by";
        public static final String ENTITY_FIELD_CREATED_BY = "createdBy";

        /**
         * 更新时刻
         */
        public static final String COLUMN_UPDATED_AT = "updated_at";
        public static final String ENTITY_FIELD_UPDATED_AT = "updatedAt";

        /**
         * 更新者
         */
        public static final String COLUMN_UPDATED_BY = "updated_by";
        public static final String ENTITY_FIELD_UPDATED_BY = "updatedBy";

        /**
         * SQL LIMIT 1
         */
        public static final String SQL_LIMIT_1 = " LIMIT 1";

    }

    public static final class Permission {
        private Permission() {
        }

        public static final String CREATE = "create";
        public static final String READ = "read";
        public static final String UPDATE = "update";
        public static final String DELETE = "delete";
    }

    public static final class Tenant {
        private Tenant() {
        }

        /**
         * 该租户ID为超级租户, 可以无视租户SQL拦截器
         */
        public static final Long DEFAULT_PRIVILEGED_TENANT_ID = 0L;
    }
}
