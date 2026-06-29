package cc.uncarbon.framework.helium.bizlog.configuration;

import cc.uncarbon.framework.helium.bizlog.support.LogRecordConfigureSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

/**
 * 启用业务日志记录
 * <p>
 * 标注在配置类上，通过 {@link Import} 引入 {@link LogRecordConfigureSelector}，
 * 进而装配 AOP 代理与各默认服务。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LogRecordConfigureSelector.class)
public @interface EnableLogRecord {

    /**
     * @return 租户标识
     */
    String tenant();

    /**
     * ！不要删掉，为 null 就不代理了哦
     * <p>
     * true：都使用 CGLIB 代理；
     * false：目标对象实现了接口——使用 JDK 动态代理机制（代理所有实现了的接口），目标对象没有接口（只有实现类）——使用 CGLIB 代理机制。
     *
     * @return 是否强制使用 CGLIB 代理
     */
    boolean proxyTargetClass() default false;

    /**
     * 指定代理通知的应用方式，默认为 {@link AdviceMode#PROXY}。
     *
     * @return 代理方式
     * @see AdviceMode
     */
    AdviceMode mode() default AdviceMode.PROXY;

    /**
     * 记录日志与业务方法是否使用同一事务。
     *
     * @return 默认独立（不加入业务事务）
     */
    boolean joinTransaction() default false;

    /**
     * 当多个通知作用于同一连接点时，事务 advisor 的执行顺序。
     * <p>
     * 默认为 {@link Ordered#LOWEST_PRECEDENCE}。
     *
     * @return 事务 advisor 的优先级
     */
    int order() default Ordered.LOWEST_PRECEDENCE;
}
