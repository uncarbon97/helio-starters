package cc.uncarbon.framework.helium.web.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.base.exception.BusinessException;
import cc.uncarbon.framework.helium.web.support.GlobalWebExceptionHandler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helium 增强 Web 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.WEB)
@Data
public class HeliumWebProperties {

    /**
     * 是否打印 {@link BusinessException} 类型异常
     */
    private boolean logBusinessException = true;

    /**
     * 是否打印有预期的异常
     * <P>有些异常已经在 {@link GlobalWebExceptionHandler} 中自动处理并返回预设文本到客户端
     * <P>开发阶段可以开启，生产环境关闭可以减少非必要的日志量
     */
    private boolean logExpectedException = true;

}
