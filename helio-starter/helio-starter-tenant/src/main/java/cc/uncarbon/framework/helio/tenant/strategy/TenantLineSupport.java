package cc.uncarbon.framework.helio.tenant.strategy;

import cc.uncarbon.framework.helio.base.constant.HelioConstant;
import cc.uncarbon.framework.helio.base.autoconfigure.HelioProperties;
import cc.uncarbon.framework.helio.tenant.context.TenantContextHolder;
import com.sun.jdi.LongValue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;

/**
 * 多租户支持-行级
 *
 * @author Uncarbon
 */
@Slf4j
public class TenantLineSupport implements TenantSupport {

    @Override
    public void support(HelioProperties helioProperties, MybatisPlusInterceptor interceptor) {
        Collection<String> ignoredTables = helioProperties.getTenant().getIgnoredTables();

        // 添加行级租户内联拦截器
        interceptor.addInnerInterceptor(
                new TenantLineInnerInterceptor(new HelioLineTenantHandler(
                        helioProperties.getTenant().getPrivilegedTenantId(), ignoredTables)
                )
        );

        log.info("\n\n[多租户支持] >> 隔离级别: 行级");

        System.err.println("以下数据表不参与租户隔离: " + ignoredTables);
    }


    /**
     * 行级租户mybatis-plus拦截器实现类
     */
    @AllArgsConstructor
    public static class HelioLineTenantHandler implements TenantLineHandler {

        /**
         * 特权租户ID
         */
        private final Long privilegedTenantId;

        /**
         * 忽略租户隔离的表
         */
        private final Collection<String> ignoredTables;


        @Override
        public Expression getTenantId() {
            Long currentTenantId = TenantContextHolder.getTenantId();
            if (currentTenantId == null) {
                return null;
            }

            return new LongValue(currentTenantId);
        }

        @Override
        public String getTenantIdColumn() {
            return HelioConstant.CRUD.COLUMN_TENANT_ID;
        }

        @Override
        public boolean ignoreTable(String tableName) {
            Long currentTenantId = TenantContextHolder.getTenantId();

            if (Objects.nonNull(privilegedTenantId)
                    && Objects.nonNull(currentTenantId)
                    && privilegedTenantId.equals(currentTenantId)) {
                return true;
            }

            return ignoredTables.contains(tableName);
        }

    }

}
