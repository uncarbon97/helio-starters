package cc.uncarbon.framework.helio.db.mybatisplus.props;

import cc.uncarbon.framework.helio.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helio 集成 mybatis-plus 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.DB_MYBATIS_PLUS)
@Data
public class HelioMybatisPlusProperties {

    /**
     * 乐观锁
     */
    private OptimisticLock optimisticLock;

    @Data
    public static final class OptimisticLock {

        /**
         * 是否启用
         */
        private Boolean enabled = false;

    }
}
