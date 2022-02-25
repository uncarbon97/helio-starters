package cc.uncarbon.framework.tenant.support;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.context.TenantContextHolder;
import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

/**
 * 多租户支持-行级
 *
 * @author Uncarbon
 */
@Slf4j
public class LineTenantSupport implements TenantSupport {

    @Override
    public void support(HelioProperties helioProperties, MybatisPlusInterceptor interceptor) {
        Collection<String> ignoredTables = helioProperties.getTenant().getIgnoredTables();

        // 添加行级租户内联拦截器
        interceptor.addInnerInterceptor(
                new TenantLineInnerInterceptor(
                        new HelioLineTenantHandler(ignoredTables)
                )
        );

        log.info("\n\n[多租户支持] >> 隔离级别: 行级，以下数据表不参与租户隔离: {}\n",
                ignoredTables);
    }


    @AllArgsConstructor
    public static class HelioLineTenantHandler implements TenantLineHandler {

        /**
         * 忽略租户隔离的表
         */
        private final Collection<String> ignoredTables;


        @Override
        public Expression getTenantId() {
            return new LongValue(TenantContextHolder.getTenantContext().getTenantId());
        }

        @Override
        public String getTenantIdColumn() {
            return HelioConstant.CRUD.COLUMN_TENANT_ID;
        }

        @Override
        public boolean ignoreTable(String tableName) {
            if (HelioConstant.CRUD.PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantContext().getTenantId())) {
                return true;
            }

            return ignoredTables.contains(tableName);
        }

    }

}
