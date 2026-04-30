package cc.uncarbon.framework.helio.ratelimiter.aop;

import cc.uncarbon.framework.helio.ratelimiter.annotation.UseRateLimit;
import cc.uncarbon.framework.helio.ratelimiter.stratrgy.RateLimitStrategy;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.BeansException;

/**
 * 限流注解关联切面
 *
 * @author ruoyi
 * @author Uncarbon
 */
@Aspect
@Slf4j
public class UseRateLimitAspect {

    @Before("@annotation(annotation)")
    public void before(JoinPoint point, UseRateLimit annotation) {
        // 确定限流策略实例
        Class<? extends RateLimitStrategy> strategyClass = annotation.strategy();
        RateLimitStrategy strategyInstance = null;
        try {
            strategyInstance = SpringUtil.getBean(strategyClass);
        } catch (BeansException be) {
            // 无法从Spring容器中得到策略类实例
        }
        if (strategyInstance == null) {
            strategyInstance = ReflectUtil.newInstance(strategyClass);
        }

        strategyInstance.performRateLimitCheck(annotation, point);
    }
}
