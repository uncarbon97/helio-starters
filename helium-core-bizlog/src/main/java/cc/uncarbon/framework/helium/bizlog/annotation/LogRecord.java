package cc.uncarbon.framework.helium.bizlog.annotation;

import java.lang.annotation.*;

/**
 * 业务日志记录注解
 * <p>
 * 标注在方法或类上，通过 AOP 拦截自动生成业务操作日志
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
     * @return 主模块，比如：订单、商品
     */
    String mainModule();

    /**
     * @return 副模块，比如：创建订单、修改商品
     */
    String subModule() default "";

    /**
     * @return 日志的操作人（支持 SpEL，留空则走操作人服务解析）
     */
    String operator() default "";

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
