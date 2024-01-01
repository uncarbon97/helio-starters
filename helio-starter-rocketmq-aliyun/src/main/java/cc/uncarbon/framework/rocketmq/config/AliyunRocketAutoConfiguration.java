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

package cc.uncarbon.framework.rocketmq.config;

import cc.uncarbon.framework.rocketmq.aspect.RocketAspect;
import cc.uncarbon.framework.rocketmq.container.RocketConsumerContainer;
import cc.uncarbon.framework.rocketmq.container.RocketProducerContainer;
import cc.uncarbon.framework.rocketmq.core.serializer.JacksonJsonSerializer;
import cc.uncarbon.framework.rocketmq.core.serializer.RocketSerializer;
import cc.uncarbon.framework.rocketmq.props.AliyunRocketProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 阿里云 Rocket MQ 自动配置类
 *
 * @author ThierrySquirrel
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {AliyunRocketProperties.class})
@RequiredArgsConstructor
@AutoConfiguration
public class AliyunRocketAutoConfiguration {

    private final AliyunRocketProperties rocketProperties;
    private final Map<String, Object> consumerContainer;
    private final RocketSerializer rocketSerializer;


    @Bean
    @ConditionalOnMissingBean(RocketConsumerContainer.class)
    public RocketConsumerContainer rocketConsumerContainer() {
        return new RocketConsumerContainer(rocketProperties, rocketSerializer);
    }

    @Bean
    @ConditionalOnMissingBean(RocketSerializer.class)
    public RocketSerializer rocketSerializer(ObjectMapper objectMapper) {
        // 使用注册于Spring的ObjectMapper
        return new JacksonJsonSerializer(objectMapper);
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
