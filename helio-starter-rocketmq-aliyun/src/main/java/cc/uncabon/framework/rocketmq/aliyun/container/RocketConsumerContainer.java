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

package cc.uncabon.framework.rocketmq.aliyun.container;

import cc.uncabon.framework.rocketmq.aliyun.core.factory.ThreadPoolFactory;
import cc.uncabon.framework.rocketmq.aliyun.core.factory.execution.ConsumerFactoryExecution;
import cc.uncabon.framework.rocketmq.aliyun.core.factory.execution.MethodFactoryExecution;
import cc.uncabon.framework.rocketmq.aliyun.core.factory.execution.ThreadPoolExecutorExecution;
import cc.uncabon.framework.rocketmq.aliyun.core.serializer.RocketSerializer;
import cc.uncabon.framework.rocketmq.aliyun.core.utils.AnnotatedMethodsUtils;
import cc.uncabon.framework.rocketmq.annotation.MessageListener;
import cc.uncabon.framework.rocketmq.annotation.RocketListener;
import cc.uncarbon.framework.core.props.AliyunRocketProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
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
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolFactory.createConsumeThreadPoolExecutor(rocketProperties);

        applicationContext.getBeansWithAnnotation(RocketListener.class).forEach((beanName, bean) -> {
            RocketListener rocketListener = bean.getClass().getAnnotation(RocketListener.class);
            AnnotatedMethodsUtils.getMethodAndAnnotation(bean, MessageListener.class).
                    forEach((method, consumerListener) -> {
                        ConsumerFactoryExecution consumerFactoryExecution = new ConsumerFactoryExecution(rocketProperties,
                                rocketListener, consumerListener, new MethodFactoryExecution(bean, method, mqSerializer));
                        ThreadPoolExecutorExecution.statsThread(threadPoolExecutor, consumerFactoryExecution);
                    });
        });

        threadPoolExecutor.shutdown();
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
