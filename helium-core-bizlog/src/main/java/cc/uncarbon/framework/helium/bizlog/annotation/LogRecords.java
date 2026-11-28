package cc.uncarbon.framework.helium.bizlog.annotation;

import java.lang.annotation.*;

/**
 * {@link LogRecord} 的容器注解
 * <p>
 * 用于在同一方法上声明多个 {@link LogRecord}
 *
 * @author wulang@mzt-biz-log
 * @author Uncarbon
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecords {
    /**
     * @return 方法上声明的多个 {@link LogRecord}
     */
    LogRecord[] value();
}
