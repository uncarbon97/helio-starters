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
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class ProviderContextFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        /*
        从 Dubbo 附件中，取出用户上下文、租户上下文等业务字段
        since 1.6.0 附件内容不再是 JSON 字符串，直接使用可序列化的对象
         */

        Object attachment = RpcContext.getServerAttachment().getObjectAttachment(UserContext.CAMEL_NAME);
        if (attachment instanceof UserContext) {
            UserContext userContext = (UserContext) attachment;
            UserContextHolder.setUserContext(userContext);
            log.debug("[Dubbo RPC] 取出当前用户上下文 >> {}", userContext);
        }

        attachment = RpcContext.getServerAttachment().getObjectAttachment(TenantContext.CAMEL_NAME);
        if (attachment instanceof TenantContext) {
            TenantContext tenantContext = (TenantContext) attachment;
            TenantContextHolder.setTenantContext(tenantContext);
            log.debug("[Dubbo RPC] 取出当前租户上下文 >> {}", tenantContext);
        }

        return invoker.invoke(invocation);
    }
}
