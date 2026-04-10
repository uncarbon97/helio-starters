package cc.uncarbon.framework.helio.jackson.props;

import cc.uncarbon.framework.helio.base.enums.BaseEnum;
import lombok.Data;

@Data
public class BaseEnumConfig {

    /**
     * 序列化 {@link BaseEnum} 类型的字段时，是否同时输出 xxxLabel
     */
    private Boolean showLabel = true;

    public boolean showLabel() {
        return Boolean.TRUE.equals(getShowLabel());
    }
}
