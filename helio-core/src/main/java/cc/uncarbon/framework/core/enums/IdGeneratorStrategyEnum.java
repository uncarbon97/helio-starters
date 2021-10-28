package cc.uncarbon.framework.core.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 主键ID生成器策略枚举类
 * @author Uncarbon
 */
@AllArgsConstructor
@Getter
public enum IdGeneratorStrategyEnum implements HelioBaseEnum<Integer> {

    /**
     * Twitter雪花算法
     */
    SNOWFLAKE(1, "SNOWFLAKE");

    private final Integer value;
    private final String label;

}
