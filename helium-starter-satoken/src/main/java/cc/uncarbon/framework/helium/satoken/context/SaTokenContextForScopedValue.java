package cc.uncarbon.framework.helium.satoken.context;

import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.context.model.SaTokenContextModelBox;
import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.servlet.model.SaRequestForServlet;
import cn.dev33.satoken.servlet.model.SaResponseForServlet;
import cn.dev33.satoken.servlet.model.SaStorageForServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 基于 {@link ScopedValue} 的 sa-token 上下文处理器，替代官方 {@code SaTokenContextForThreadLocal}。
 *
 * <p>把 {@link SaTokenContextModelBox} 绑定在 {@link ScopedValue} 作用域内，兼容虚拟线程；
 * 作用域内启动的子线程（结构化并发 / 虚拟线程）会自动继承绑定
 *
 * @author Uncarbon
 */
public final class SaTokenContextForScopedValue implements SaTokenContext {

    private static final ScopedValue<SaTokenContextModelBox> SCOPED = ScopedValue.newInstance();

    /**
     * 暴露底层 {@link ScopedValue}，供测试或外部绑定使用
     */
    public static ScopedValue<SaTokenContextModelBox> scoped() {
        return SCOPED;
    }

    /**
     * 便捷绑定，返回已绑定 box 的 {@link ScopedValue.Carrier}
     */
    public static ScopedValue.Carrier where(SaTokenContextModelBox box) {
        return ScopedValue.where(SCOPED, box);
    }

    /**
     * 工具方法，创建 {@link SaTokenContextModelBox}
     */
    public static SaTokenContextModelBox boxOf(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        SaRequest req = new SaRequestForServlet(servletRequest);
        SaResponse res = new SaResponseForServlet(servletResponse);
        SaStorage stg = new SaStorageForServlet(servletRequest);
        return new SaTokenContextModelBox(req, res, stg);
    }

    @Override
    public void setContext(SaRequest req, SaResponse res, SaStorage stg) {
        // ScopedValue 不可命令式写入，绑定仅通过 where(box) 在作用域内完成
    }

    @Override
    public void clearContext() {
        // ScopedValue 退出作用域自动清理，无需手动清除
    }

    @Override
    public boolean isValid() {
        return SCOPED.isBound();
    }

    @Override
    public SaTokenContextModelBox getModelBox() {
        if (!SCOPED.isBound()) {
            throw new SaTokenContextException("SaTokenContext 上下文尚未初始化").setCode(SaErrorCode.CODE_10002);
        }
        return SCOPED.get();
    }
}
