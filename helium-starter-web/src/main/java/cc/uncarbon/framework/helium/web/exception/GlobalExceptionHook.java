package cc.uncarbon.framework.helium.web.exception;

/**
 * 全局异常处理钩子
 *
 * <P>由下游应用声明为 Spring Bean 即可生效，支持多个实现，使用 {@code @Order} 控制执行顺序。
 * <P>钩子在框架默认日志打印之前触发；若任一钩子返回 {@link Verdict#SKIP_DEFAULT_LOG}，框架将跳过默认日志打印。
 * <P>钩子内的异常会被框架捕获并单独记录，不会影响正常的错误响应。
 *
 * <P>示例：集成 Sentry，只上报未归类异常
 * <pre>{@code
 * @Component
 * public class SentryExceptionHook implements GlobalExceptionHook {
 *
 *     @Override
 *     public Verdict onException(ExceptionHookContext context) {
 *         if (context.category() == ExceptionCategory.UNEXPECTED) {
 *             Sentry.captureException(context.exception());
 *         }
 *         return Verdict.CONTINUE;
 *     }
 * }
 * }</pre>
 *
 * @author Uncarbon
 */
public interface GlobalExceptionHook {

    /**
     * 异常触发回调
     *
     * @param context 钩子上下文
     * @return 处理结果
     */
    Verdict onException(ExceptionHookContext context);

    /**
     * 钩子处理结果
     */
    enum Verdict {

        /**
         * 继续框架默认行为（含默认日志打印）
         */
        CONTINUE,

        /**
         * 跳过框架默认日志打印（常见于下游自行记录日志、或不想重复输出日志的场景）
         */
        SKIP_DEFAULT_LOG
    }
}
