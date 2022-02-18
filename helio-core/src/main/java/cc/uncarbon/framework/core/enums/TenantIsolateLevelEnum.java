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

    /*
    未实现
    DATASOURCE(4, "数据源级(即每个租户单独一个数据库)");
     */

    /**
     * 行级，即每张表增加一个'租户ID'字段
     */
    LINE(1, "行级");

    private final Integer value;
    private final String label;

}
