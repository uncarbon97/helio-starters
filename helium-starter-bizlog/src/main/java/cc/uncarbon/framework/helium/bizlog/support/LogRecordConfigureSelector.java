package cc.uncarbon.framework.helium.bizlog.support;

import cc.uncarbon.framework.helium.bizlog.configuration.LogRecordProxyAutoConfiguration;
import cc.uncarbon.framework.helium.bizlog.configuration.EnableLogRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

/**
 * 业务日志装配选择器
 * <p>
 * 根据代理模式（PROXY / ASPECTJ）选择需要导入的配置类；
 * PROXY 模式额外导入 {@link AutoProxyRegistrar} 以启用 Spring AOP 自动代理。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public class LogRecordConfigureSelector extends AdviceModeImportSelector<EnableLogRecord> {

    /**
     * 按代理模式选择导入的配置类全限定名。
     *
     * @param adviceMode 代理模式
     * @return 需导入的配置类全限定名数组
     */
    @Override
    @Nullable
    public String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return new String[]{AutoProxyRegistrar.class.getName(), LogRecordProxyAutoConfiguration.class.getName()};
            case ASPECTJ:
                return new String[] {LogRecordProxyAutoConfiguration.class.getName()};
            default:
                return null;
        }
    }
}
