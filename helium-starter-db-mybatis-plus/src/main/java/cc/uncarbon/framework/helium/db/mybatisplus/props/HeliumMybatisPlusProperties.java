package cc.uncarbon.framework.helium.db.mybatisplus.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helium 集成 mybatis-plus 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.DB_MYBATIS_PLUS)
@Data
public class HeliumMybatisPlusProperties {

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
