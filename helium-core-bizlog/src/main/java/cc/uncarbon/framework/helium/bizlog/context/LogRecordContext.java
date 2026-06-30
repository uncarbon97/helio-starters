package cc.uncarbon.framework.helium.bizlog.context;

import cc.uncarbon.framework.helium.bizlog.annotation.LogRecord;
import lombok.experimental.UtilityClass;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 业务日志记录上下文
 * <p>
 * 基于 {@link ScopedValue} 实现，替代原 {@link InheritableThreadLocal} 方案，
 * 支持在虚拟线程及结构化并发场景下正确传递上下文
 * <p>
 * 上下文包含两部分：
 * <ul>
 *   <li>方法级变量栈 {@code variableMapStack}：每个 {@link LogRecord} 方法进入时压入一帧，方法结束时弹出，互不污染；</li>
 *   <li>全局变量 {@code globalVariableMap}：在当前顶层作用域内跨方法共享。</li>
 * </ul>
 * 作用域由 {@code LogRecordInterceptor} 在拦截入口通过 {@link #callWithContextThrowing} 绑定，
 * 退出时由 {@link ScopedValue} 自动解绑，无需手动 {@code remove()}，亦不会在线程池残留。
 * <p>
 * 说明：{@link ScopedValue} 绑定的是固定的 {@link ContextState} 实例，
 * 对内部集合的 push/pop/put 属于同一线程内的同步操作（拦截器与业务代码同线程执行，且日志解析不在方法执行期间派生并发任务），因此不存在并发写竞争；
 * 在绑定作用域内由业务代码派生的虚拟线程可自动继承绑定，从而读取上下文。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@UtilityClass
public class LogRecordContext {

    /**
     * 当前作用域绑定的上下文状态，替代原 {@link InheritableThreadLocal}，支持虚拟线程传递。
     */
    private static final ScopedValue<ContextState> SCOPED = ScopedValue.newInstance();

    /**
     * 在新的日志上下文作用域中执行无返回值操作，结束自动解绑。
     *
     * @param op 作用域内执行的操作
     */
    public static void runWithContext(Runnable op) {
        ScopedValue.where(SCOPED, new ContextState()).run(op);
    }

    /**
     * 在新的日志上下文作用域中执行有返回值操作，结束自动解绑。
     *
     * @param op 作用域内执行的操作
     * @param <T> 返回值类型
     * @return 操作返回值
     * @throws Exception 操作抛出的受检异常
     */
    public static <T> T callWithContext(Callable<T> op) throws Exception {
        return ScopedValue.where(SCOPED, new ContextState()).call(op::call);
    }

    /**
     * 在新的日志上下文作用域中执行操作，并完整透传 {@link Throwable}。
     * <p>
     * 因 {@code MethodInvocation.proceed()} 声明抛出 {@link Throwable}，
     * 而 {@link Callable#call()} 仅声明抛出 {@link Exception}，
     * 故采用「结果槽 + 异常槽 + {@link ScopedValue#run(Runnable)}」模式以保留任意 {@link Throwable}。
     *
     * @param op 作用域内执行的操作
     * @param <T> 返回值类型
     * @return 操作返回值
     * @throws Throwable 操作抛出的任意异常
     */
    public static <T> T callWithContextThrowing(ThrowingSupplier<T> op) throws Throwable {
        final Object[] result = new Object[1];
        final Throwable[] error = new Throwable[1];
        ScopedValue.where(SCOPED, new ContextState()).run(() -> {
            try {
                result[0] = op.get();
            } catch (Throwable t) {
                error[0] = t;
            }
        });
        if (error[0] != null) {
            throw error[0];
        }
        @SuppressWarnings("unchecked")
        T ret = (T) result[0];
        return ret;
    }

    /**
     * 当前线程是否已绑定日志上下文。
     *
     * @return 已绑定返回 {@code true}
     */
    public static boolean isBound() {
        return SCOPED.isBound();
    }

    /**
     * 写入方法级变量到当前栈帧；若当前未绑定作用域则忽略。
     *
     * @param name  变量名
     * @param value 变量值
     */
    public static void putVariable(String name, Object value) {
        if (!isBound()) {
            return;
        }
        Deque<Map<String, Object>> stack = currentState().variableMapStack;
        if (stack.isEmpty()) {
            stack.push(new HashMap<>());
        }
        stack.element().put(name, value);
    }

    /**
     * 写入全局变量；若当前未绑定作用域则忽略。
     *
     * @param name  变量名
     * @param value 变量值
     */
    public static void putGlobalVariable(String name, Object value) {
        if (!isBound()) {
            return;
        }
        currentState().globalVariableMap.put(name, value);
    }

    /**
     * 从当前栈帧读取变量。
     *
     * @param key 变量名
     * @return 变量值，未绑定或不存在时返回 {@code null}
     */
    public static Object getVariable(String key) {
        if (!isBound()) {
            return null;
        }
        Map<String, Object> variableMap = currentState().variableMapStack.peek();
        return variableMap == null ? null : variableMap.get(key);
    }

    /**
     * 先查方法级变量，未命中再查全局变量。
     *
     * @param key 变量名
     * @return 变量值，未绑定或不存在时返回 {@code null}
     */
    public static Object getMethodOrGlobal(String key) {
        if (!isBound()) {
            return null;
        }
        Object result = null;
        Map<String, Object> variableMap = currentState().variableMapStack.peek();
        if (variableMap != null && !variableMap.isEmpty() && (result = variableMap.get(key)) != null) {
            return result;
        }
        Map<String, Object> globalMap = currentState().globalVariableMap;
        if (!globalMap.isEmpty()) {
            return globalMap.get(key);
        }
        return result;
    }

    /**
     * 获取当前栈帧的变量表。
     *
     * @return 当前栈帧变量表；未绑定时返回空表
     */
    public static Map<String, Object> getVariables() {
        if (!isBound()) {
            return new HashMap<>();
        }
        return currentState().variableMapStack.peek();
    }

    /**
     * 获取全局变量表。
     *
     * @return 全局变量表；未绑定时返回 {@code null}
     */
    public static Map<String, Object> getGlobalVariableMap() {
        return isBound() ? currentState().globalVariableMap : null;
    }

    /**
     * 弹出当前方法栈帧；未绑定时忽略。
     */
    public static void clear() {
        if (isBound()) {
            Deque<Map<String, Object>> stack = currentState().variableMapStack;
            if (!stack.isEmpty()) {
                stack.pop();
            }
        }
    }

    /**
     * 清空全局变量；未绑定时忽略。
     */
    public static void clearGlobal() {
        if (isBound()) {
            currentState().globalVariableMap.clear();
        }
    }

    /**
     * 压入一个新的空栈帧。
     * <p>
     * 日志使用方一般无需调用此方法；由 {@code LogRecordInterceptor} 在进入每个 {@code @LogRecord} 方法时调用，
     * 方法执行完毕后在 {@code finally} 中调用 {@link #clear()} 弹出对应栈帧。
     */
    public static void putEmptySpan() {
        if (!isBound()) {
            return;
        }
        currentState().variableMapStack.push(new HashMap<>());
    }

    /**
     * 取当前绑定的上下文状态；调用方需自行保证 {@link #isBound()} 为真。
     *
     * @return 当前上下文状态
     */
    private static ContextState currentState() {
        return SCOPED.get();
    }

    /**
     * 上下文内部状态：方法级变量栈 + 全局变量表。
     * <p>
     * 字段引用为 final，内部集合可变，由 {@link ScopedValue} 绑定，作用域内单线程同步读写。
     */
    private static final class ContextState {

        private final Deque<Map<String, Object>> variableMapStack = new ArrayDeque<>();

        private final Map<String, Object> globalVariableMap = new HashMap<>();
    }

    /**
     * 可抛出任意 {@link Throwable} 的供应者，用于 {@link #callWithContextThrowing}。
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {

        /**
         * 计算并返回结果。
         *
         * @return 结果
         * @throws Throwable 任意异常
         */
        T get() throws Throwable;
    }
}
