package cc.uncarbon.framework.dubbo.filter;

import cc.uncarbon.framework.core.context.TenantContext;
import cc.uncarbon.framework.core.context.TenantContextHolder;
import cc.uncarbon.framework.core.context.UserContext;
import cc.uncarbon.framework.core.context.UserContextHolder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

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
        String userContextJson = RpcContext.getContext().getAttachment(UserContext.CAMEL_NAME);
        if (StrUtil.isNotEmpty(userContextJson)) {
            UserContext userContext = JSONUtil.parseObj(userContextJson).toBean(UserContext.class);
            UserContextHolder.setUserContext(userContext);
            log.debug("[Dubbo RPC] 获取当前用户上下文 >> {}", userContextJson);
        }

        if (TenantContextHolder.isTenantEnabled()) {
            // 启用了多租户的前提下，才获取
            String tenantContextJson = RpcContext.getContext().getAttachment(TenantContext.CAMEL_NAME);
            if (StrUtil.isNotEmpty(tenantContextJson)) {
                TenantContext tenantContext = JSONUtil.parseObj(tenantContextJson).toBean(TenantContext.class);
                TenantContextHolder.setTenantContext(tenantContext);
                log.debug("[Dubbo RPC] 获取当前租户上下文 >> {}", tenantContextJson);
            }
        }

        return invoker.invoke(invocation);
    }
}
