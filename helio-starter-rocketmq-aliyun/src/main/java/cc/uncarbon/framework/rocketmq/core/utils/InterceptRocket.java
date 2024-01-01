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

package cc.uncarbon.framework.rocketmq.core.utils;

import cc.uncarbon.framework.rocketmq.annotation.RocketMessage;
import cc.uncarbon.framework.rocketmq.core.factory.execution.SendMessageFactoryExecution;
import cc.uncarbon.framework.rocketmq.core.factory.execution.ThreadPoolExecutorExecution;
import cc.uncarbon.framework.rocketmq.core.serializer.RocketSerializer;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ClassName: InterceptRocket
 * Description:
 * date: 2019/4/29 22:03
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class InterceptRocket {
    private InterceptRocket() {
    }

    @SuppressWarnings("squid:S107")
    public static <T extends Annotation> Object intercept(Long startDeliverTime, String shardingKeyFactory, RocketMessage rocketMessage, T annotation, Object proceed, Map<String, Object> consumerContainer, ThreadPoolExecutor threadPoolExecutor, ApplicationContext applicationContext) {
        RocketSerializer mqSerializer = applicationContext.getBean(RocketSerializer.class);
        byte[] body = mqSerializer.serialize(proceed);

        ThreadPoolExecutorExecution.statsThread(threadPoolExecutor, new SendMessageFactoryExecution(startDeliverTime, shardingKeyFactory, consumerContainer, rocketMessage, annotation, body, applicationContext));
        return proceed;
    }
}
