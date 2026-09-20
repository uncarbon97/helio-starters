package cc.uncarbon.framework.helium.tenant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 多租户登录模式枚举
 * 决定登录时租户的确定时机
 *
 * @author Uncarbon
 */
@AllArgsConstructor
@Getter
public enum TenantLoginModeEnum {

    /**
     * 租户优先模式：先创建租户，后加入用户（国内主流后台形态，同 pin 可跨租户共存）
     * <p>登录时需要提供租户编码</p>
     */
    TENANT_FIRST,

    /**
     * 用户优先模式：先创建用户，后加入租户（钉钉、飞书等形态，pin 全局唯一）
     * <p>登录免填租户编码，按用户-租户关联关系推导，可属多个租户</p>
     */
    USER_FIRST,

}
