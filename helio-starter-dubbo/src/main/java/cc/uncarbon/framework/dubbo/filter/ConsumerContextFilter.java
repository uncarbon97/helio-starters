package cc.uncarbon.framework.dubbo.filter;

import cc.uncarbon.framework.core.context.TenantContextHolder;
import cc.uncarbon.framework.core.context.UserContext;
import cc.uncarbon.framework.core.context.UserContextHolder;
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
        String userContextJson = JSONUtil.toJsonStr(UserContextHolder.getUserContext());
        String tenantContextJson = JSONUtil.toJsonStr(TenantContextHolder.getTenantContext());

        // 放进 Dubbo 消费者附件中
        log.debug("[Dubbo RPC] 设置当前用户上下文 >> {}", userContextJson);
        RpcContext.getContext().setAttachment(UserContext.CAMEL_NAME, userContextJson);

        return invoker.invoke(invocation);
    }

}
