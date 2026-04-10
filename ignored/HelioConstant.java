package cc.uncarbon.framework.helio.base.constant;

/**
 * Helio 脚手架基本常量
 *
 * @author Uncarbon
 */
public final class HelioConstant {
    private HelioConstant() {
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

}
