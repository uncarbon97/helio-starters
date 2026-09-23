package cc.uncarbon.framework.helium.tenant.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HeliumTenantConstant {

    /**
     * 仅用于「无租户上下文时的兜底写值」（如平台自营域），不用于登录回退
     */
    public static long FALLBACK_TENANT_ID = 0;

}
