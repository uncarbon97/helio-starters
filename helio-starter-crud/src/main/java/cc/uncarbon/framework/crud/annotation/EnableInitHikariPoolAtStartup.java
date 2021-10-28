package cc.uncarbon.framework.crud.annotation;

import cc.uncarbon.framework.crud.config.InitHikariPoolAtStartupConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 在项目启动时直接初始化Hikari连接池, 关闭按需连接
 * 参考https://github.com/spring-projects/spring-boot/issues/19596
 * @author Uncarbon
 */
@Import({InitHikariPoolAtStartupConfiguration.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableInitHikariPoolAtStartup {
}
