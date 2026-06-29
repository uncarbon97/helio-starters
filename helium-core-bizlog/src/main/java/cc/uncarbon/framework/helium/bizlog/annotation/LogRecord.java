package cc.uncarbon.framework.helium.bizlog.annotation;

import java.lang.annotation.*;

/**
 * 业务日志记录注解
 * <p>
 * 标注在方法或类上，通过 AOP 拦截自动生成业务操作日志；
 * 支持 {@link Repeatable}，同一方法可声明多个以对应不同模板与条件。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Repeatable(LogRecords.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecord {
    /**
     * @return 方法执行成功后的日志模版
     */
    String success();

    /**
     * @return 方法执行失败后的日志模版
     */
    String fail() default "";

    /**
     * @return 日志的操作人（支持 SpEL，留空则走操作人服务解析）
     */
    String operator() default "";

    /**
     * @return 操作日志的类型，比如：订单类型、商品类型
     */
    String type();

    /**
     * @return 日志的子类型，比如订单的C端日志，和订单的B端日志，type都是订单类型，但是子类型不一样
     */
    String subType() default "";

    /**
     * @return 日志绑定的业务标识
     */
    String bizNo();

    /**
     * @return 日志的额外信息
     */
    String extra() default "";

    /**
     * @return 是否记录日志的条件（SpEL，留空表示始终记录）
     */
    String condition() default "";

    /**
     * 记录成功日志的条件。
     *
     * @return 表示成功的表达式，默认为空，代表不抛异常即为成功
     */
    String successCondition() default "";
}
