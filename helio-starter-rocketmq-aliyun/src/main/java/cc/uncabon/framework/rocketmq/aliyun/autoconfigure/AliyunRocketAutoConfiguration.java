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

package cc.uncabon.framework.rocketmq.aliyun.autoconfigure;

import cc.uncabon.framework.rocketmq.aliyun.aspect.RocketAspect;
import cc.uncabon.framework.rocketmq.aliyun.container.RocketConsumerContainer;
import cc.uncabon.framework.rocketmq.aliyun.container.RocketProducerContainer;
import cc.uncabon.framework.rocketmq.aliyun.core.serializer.Base64Serializer;
import cc.uncabon.framework.rocketmq.aliyun.core.serializer.RocketSerializer;
import cc.uncarbon.framework.core.props.AliyunRocketProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * ClassName: RocketProperties
 * Description:
 * date: 2019/4/25 15:57
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Configuration
public class AliyunRocketAutoConfiguration {

    @Resource
    private AliyunRocketProperties rocketProperties;

    @Resource
    private Map<String, Object> consumerContainer;

    @Resource
    private RocketSerializer rocketSerializer;


    @Bean
    @ConditionalOnMissingBean(RocketConsumerContainer.class)
    public RocketConsumerContainer rocketConsumerContainer() {
        return new RocketConsumerContainer(rocketProperties, rocketSerializer);
    }

    @Bean
    @ConditionalOnMissingBean(RocketSerializer.class)
    public RocketSerializer rocketSerializer() {
        return new Base64Serializer();
    }

    @Bean
    @ConditionalOnMissingBean(Map.class)
    public Map<String, Object> consumerContainer() {
        return new ConcurrentHashMap<>(32);
    }

    @Bean
    @ConditionalOnMissingBean(RocketProducerContainer.class)
    public RocketProducerContainer rocketProducerContainer() {
        return new RocketProducerContainer(consumerContainer, rocketProperties);
    }

    @Bean
    @ConditionalOnMissingBean(RocketAspect.class)
    public RocketAspect rockerAspect() {
        return new RocketAspect(consumerContainer, rocketProperties);
    }
}
