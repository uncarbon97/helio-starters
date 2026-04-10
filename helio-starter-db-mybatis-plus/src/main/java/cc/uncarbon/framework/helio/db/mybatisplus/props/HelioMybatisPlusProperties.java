package cc.uncarbon.framework.helio.db.mybatisplus.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helio.db.mybatis-plus")
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
