package cc.uncarbon.framework.dubbo.filter;

import cc.uncarbon.framework.core.context.TenantContext;
import cc.uncarbon.framework.core.context.TenantContextHolder;
import cc.uncarbon.framework.core.context.UserContext;
import cc.uncarbon.framework.core.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * 自定义Dubbo提供者上下文
 *
 * @author Zhu JW
 * @author Uncarbon
 **/
@Activate(group = CommonConstants.PROVIDER)
@Slf4j
public class ProviderContextFilter implements Filter {

    /**
     * 从 Dubbo 附件中，取出用户上下文、租户上下文等业务字段
     * @since 1.6.0 附件内容不再是 JSON 字符串，直接使用可序列化的对象
     * @since 1.8.0 判断isProviderSide，非提供者不从附件取值
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContextAttachment serverAttachment = RpcContext.getServerAttachment();
        if (serverAttachment.isProviderSide()) {
            // 清理数据，避免线程池化复用残留
            UserContextHolder.clear();
            TenantContextHolder.clear();

            Object attachment = serverAttachment.getObjectAttachment(UserContext.CAMEL_NAME);
            if (attachment instanceof UserContext userContext) {
                UserContextHolder.setUserContext(userContext);
                log.debug("[Dubbo RPC] 取出当前用户上下文 >> {}", userContext);
            }

            attachment = serverAttachment.getObjectAttachment(TenantContext.CAMEL_NAME);
            if (attachment instanceof TenantContext tenantContext) {
                TenantContextHolder.setTenantContext(tenantContext);
                log.debug("[Dubbo RPC] 取出当前租户上下文 >> {}", tenantContext);
            }
        }

        return invoker.invoke(invocation);
    }
}
