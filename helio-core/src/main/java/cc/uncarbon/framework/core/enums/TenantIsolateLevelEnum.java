package cc.uncarbon.framework.core.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 多租户隔离级别枚举类
 * @author Uncarbon
 */
@AllArgsConstructor
@Getter
public enum TenantIsolateLevelEnum implements HelioBaseEnum<Integer> {

    /**
     * 行级，即每张表增加一个'租户ID'字段
     */
    LINE(1, "行级"),

    /**
     * 表级，即表名增加[_租户ID]后缀用于区分，如 tb_member_10001 tb_member_10002
     */
    TABLE(2, "表级"),

    /**
     * ！！！未实现！！！
     * 数据源级，即每个租户使用独立的数据源
     */
    DATASOURCE(3, "数据源级"),

    ;

    private final Integer value;
    private final String label;

}
