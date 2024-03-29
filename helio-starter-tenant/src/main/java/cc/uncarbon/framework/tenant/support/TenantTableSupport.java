package cc.uncarbon.framework.tenant.support;

import cc.uncarbon.framework.core.context.TenantContextHolder;
import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 多租户支持-表级
 * @deprecated 后来发现并不是太适合实践使用，仅保留本类
 *
 * @author Uncarbon
 */
@Slf4j
@Deprecated
public class TenantTableSupport implements TenantSupport {

    @Override
    public void support(HelioProperties helioProperties, MybatisPlusInterceptor interceptor) {
        Collection<String> ignoredTables = helioProperties.getTenant().getIgnoredTables();

        DynamicTableNameInnerInterceptor innerInterceptor = new DynamicTableNameInnerInterceptor();
        innerInterceptor.setTableNameHandler(new HelioTableTenantHandler(ignoredTables));

        // 添加表级租户内联拦截器
        interceptor.addInnerInterceptor(innerInterceptor);

        log.info("\n\n[多租户支持] >> 隔离级别: 表级，以下数据表不参与租户隔离: {}\n",
                ignoredTables);
    }

    /**
     * 表级租户mybatis-plus拦截器实现类
     */
    @AllArgsConstructor
    public static class HelioTableTenantHandler implements TableNameHandler {

        /**
         * 忽略租户隔离的表
         */
        private Collection<String> ignoredTables;

        @Override
        public String dynamicTableName(String sql, String tableName) {
            if (ignoredTables.contains(tableName)) {
                return tableName;
            }

            // 拼接新表名
            return String.format("%s_%s", tableName, TenantContextHolder.getTenantId());
        }

    }
}
