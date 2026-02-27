package cc.uncarbon.framework.helio.core.context;

public interface TenantContext {

    String CAMEL_NAME = "tenantContext";

    /**
     * 取得租户ID
     */
    Long getTenantId();

    /**
     * 取得租户名称
     */
    String getTenantName();

    /**
     * 取得租户编码
     */
    String getTenantCode();

}
