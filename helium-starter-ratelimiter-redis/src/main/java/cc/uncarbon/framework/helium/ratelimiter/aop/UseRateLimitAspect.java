package cc.uncarbon.framework.helium.ratelimiter.aop;

import cc.uncarbon.framework.helium.ratelimiter.annotation.UseRateLimit;
import cc.uncarbon.framework.helium.ratelimiter.stratrgy.RateLimitStrategy;
import cn.hutool.extra.spring.SpringUtil;
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
        RateLimitStrategy strategyInstance;
        try {
            strategyInstance = SpringUtil.getBean(strategyClass);
        } catch (BeansException be) {
            strategyInstance = null;
        }
        if (strategyInstance == null) {
            throw new IllegalStateException("限流策略 " + strategyClass.getName()
                    + " 未注册为 Spring Bean，无法实例化");
        }
        strategyInstance.performRateLimitCheck(annotation, point);
    }
}
