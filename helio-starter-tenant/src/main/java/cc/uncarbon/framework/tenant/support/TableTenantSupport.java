package cc.uncarbon.framework.tenant.support;

import cc.uncarbon.framework.core.context.TenantContextHolder;
import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 多租户支持-表级
 */
@Slf4j
public class TableTenantSupport implements TenantSupport {

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


    @AllArgsConstructor
    public static class HelioTableTenantHandler implements TableNameHandler {

        /**
         * 忽略租户隔离的表
         */
        private final Collection<String> ignoredTables;


        @Override
        public String dynamicTableName(String sql, String tableName) {
            if (ignoredTables.contains(tableName)) {
                return tableName;
            }

            // 拼接新表名
            return String.format("%s_%s", tableName, TenantContextHolder.getTenantContext().getTenantId());
        }

    }
}
