package cc.uncarbon.framework.core.constant;


import java.util.Locale;

public interface HelioConstant {
    interface Dubbo {
        String ENABLE_VALIDATION = "true";
        int TIMEOUT = 10000;
        int RETRIES = -1;
        int RPC_EXCEPTION_RESPONSE_CODE = 1;
    }

    interface Message {
        String NULL = "暂无数据";
        String SUCCESS = "操作成功";
    }

    interface Version {
        /**
         * replaced with HTTP_API_VERSION_V1
         */
        @Deprecated
        String APP_API_VERSION_V1 = "/api/v1";

        /**
         * replaced with HTTP_API_VERSION_V1
         */
        @Deprecated
        String SAAS_API_VERSION_V1 = "/api/v1";

        /**
         * HTTP API 版本 v1
         */
        String HTTP_API_VERSION_V1 = "/api/v1";

        /**
         * DUBBO API 版本 v1
         */
        String DUBBO_VERSION_V1 = "1.0.0";
    }

    interface Jackson {
        Locale LOCALE = Locale.CHINA;
        String TIME_ZONE = "GMT+8";
        String DATE_FORMAT = "yyyy-MM-dd";
        String TIME_FORMAT = "HH:mm:ss";
        String DATE_TIME_FORMAT = Jackson.DATE_FORMAT + " " + Jackson.TIME_FORMAT;
    }

    interface CRUD {
        Long DEFAULT_TENANT_ID = 0L;
        String DEFAULT_TENANT_NAME = "请设置租户ID";

        /**
         * 该租户ID为超级租户, 可以无视租户SQL拦截器
         */
        Long PRIVILEGED_TENANT_ID = 0L;

        /**
         * 租户ID
         */
        String COLUMN_TENANT_ID = "tenant_id";
        String ENTITY_FIELD_TENANT_ID = "tenantId";

        /**
         * 创建时刻
         */
        String COLUMN_CREATED_AT = "created_at";
        String ENTITY_FIELD_CREATED_AT = "createdAt";

        /**
         * 创建者
         */
        String COLUMN_CREATED_BY = "created_by";
        String ENTITY_FIELD_CREATED_BY = "createdBy";

        /**
         * 更新时刻
         */
        String COLUMN_UPDATED_AT = "updated_at";
        String ENTITY_FIELD_UPDATED_AT = "updatedAt";

        /**
         * 更新者
         */
        String COLUMN_UPDATED_BY = "updated_by";
        String ENTITY_FIELD_UPDATED_BY = "updatedBy";

        /**
         * SQL LIMIT 1
         */
        String SQL_LIMIT_1 = " LIMIT 1";

    }

    interface Regex {
        String CHINA_MAINLAND_PHONE_NO = "^[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][5,6])|([7][0-8])|([8][0-9])|([9][1,8,9]))[0-9]{8}$";
        String EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    }

    interface Permission {
        String CREATE = "create";
        String RETRIEVE = "retrieve";
        String UPDATE = "update";
        String DELETE = "delete";
    }
}
