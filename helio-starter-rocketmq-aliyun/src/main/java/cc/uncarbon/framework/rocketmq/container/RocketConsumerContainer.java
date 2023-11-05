/**
 * Copyright 2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.uncarbon.framework.rocketmq.container;

import cc.uncarbon.framework.rocketmq.annotation.MessageListener;
import cc.uncarbon.framework.rocketmq.annotation.RocketListener;
import cc.uncarbon.framework.rocketmq.core.factory.ThreadPoolFactory;
import cc.uncarbon.framework.rocketmq.core.factory.execution.ConsumerFactoryExecution;
import cc.uncarbon.framework.rocketmq.core.factory.execution.MethodFactoryExecution;
import cc.uncarbon.framework.rocketmq.core.factory.execution.ThreadPoolExecutorExecution;
import cc.uncarbon.framework.rocketmq.core.serializer.RocketSerializer;
import cc.uncarbon.framework.rocketmq.core.utils.AnnotatedMethodsUtils;
import cc.uncarbon.framework.rocketmq.props.AliyunRocketProperties;
import lombok.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ClassName: RocketConsumerContainer
 * Description:
 * date: 2019/4/26 21:40
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class RocketConsumerContainer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final AliyunRocketProperties rocketProperties;

    private final RocketSerializer mqSerializer;


    public RocketConsumerContainer(AliyunRocketProperties rocketProperties, RocketSerializer rocketSerializer) {
        this.rocketProperties = rocketProperties;
        this.mqSerializer = rocketSerializer;
    }

    @PostConstruct
    public void initialize() {
        // 创建临时线程池
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolFactory.createConsumeThreadPoolExecutor(rocketProperties);

        // 扫描标记 @RocketListener 注解的类，再将其中标记了 @MessageListener 的方法包装一下
        // 依次注册到阿里云SDK
        applicationContext.getBeansWithAnnotation(RocketListener.class).forEach((beanName, bean) -> {
            RocketListener rocketListener = bean.getClass().getAnnotation(RocketListener.class);
            AnnotatedMethodsUtils.getMethodAndAnnotation(bean, MessageListener.class).
                    forEach((method, consumerListener) -> {
                        ConsumerFactoryExecution consumerFactoryExecution = new ConsumerFactoryExecution(rocketProperties,
                                rocketListener, consumerListener, new MethodFactoryExecution(bean, method, mqSerializer));
                        ThreadPoolExecutorExecution.statsThread(threadPoolExecutor, consumerFactoryExecution);
                    });
        });

        // 销毁临时线程池
        threadPoolExecutor.shutdown();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
