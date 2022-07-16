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
 * 自定义Dubbo消费者上下文
 *
 * @author Zhu JW
 * @author Uncarbon
 **/
@Slf4j
@Activate(group = CommonConstants.CONSUMER)
public class ConsumerContextFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        /*
        将用户上下文、租户上下文等业务字段，放进 Dubbo 附件中
        @since 1.6.0 不再转换成 JSON 字符串后放入附件中，直接使用可序列化的对象
         */

        UserContext userContext = UserContextHolder.getUserContext();
        if (userContext != null) {
            log.debug("[Dubbo RPC] 设置当前用户上下文 >> {}", userContext);
            RpcContext.getClientAttachment().setAttachment(UserContext.CAMEL_NAME, userContext);
        }

        TenantContext tenantContext = TenantContextHolder.getTenantContext();
        if (tenantContext != null && tenantContext.getTenantId() != null) {
            // 实际启用了租户
            log.debug("[Dubbo RPC] 设置当前租户上下文 >> {}", tenantContext);
            RpcContext.getClientAttachment().setAttachment(TenantContext.CAMEL_NAME, tenantContext);
        }

        return invoker.invoke(invocation);
    }

}
