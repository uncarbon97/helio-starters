package cc.uncarbon.framework.helium.jackson.props;

import cc.uncarbon.framework.helium.base.enums.BaseEnum;
import lombok.Data;

@Data
public class BaseEnumConfig {

    /**
     * 序列化 {@link BaseEnum} 类型的字段时，是否同时输出 xxxLabel
     */
    private Boolean showLabel = Boolean.TRUE;

    public boolean showLabel() {
        return Boolean.TRUE.equals(getShowLabel());
    }
}
