package cc.uncarbon.framework.helium.tenant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 多租户策略枚举
 *
 * @author Uncarbon
 */
@AllArgsConstructor
@Getter
public enum TenantStrategyEnum {

    /**
     * 不使用多租户
     */
    NONE,

    /**
     * 行级，即每张表增加一个【租户ID】字段
     */
    LINE,

    /**
     * 数据源级，即每个租户使用独立的数据源
     */
    DATASOURCE,

    /**
     * 自定义策略
     */
    CUSTOM,
}
