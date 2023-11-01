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

import cc.uncarbon.framework.rocketmq.annotation.RocketMessage;
import cc.uncarbon.framework.rocketmq.core.factory.ThreadPoolFactory;
import cc.uncarbon.framework.rocketmq.core.strategy.RocketConsumerStrategy;
import cc.uncarbon.framework.rocketmq.props.AliyunRocketProperties;
import lombok.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ClassName: RocketProducerContainer
 * Description:
 * date: 2019/5/3 11:29
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class RocketProducerContainer implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	private final AliyunRocketProperties rocketProperties;

	private final Map<String, Object> consumerContainer;


	public RocketProducerContainer(Map<String, Object> consumerContainer, AliyunRocketProperties rocketProperties) {
		this.consumerContainer = consumerContainer;
		this.rocketProperties = rocketProperties;
	}

	@PostConstruct
	public void initialize() {
		// 创建临时线程池
		ThreadPoolExecutor threadPoolExecutor = ThreadPoolFactory.createProducerThreadPoolExecutor(rocketProperties);
		// 扫描标记 @RocketMessage 注解的类，再将其中标记了 @CommonMessage/@OrderMessage/@TransactionMessage 的方法包装一下
		// 依次注册到阿里云SDK
		applicationContext.getBeansWithAnnotation(RocketMessage.class).forEach((beanName, bean) -> RocketConsumerStrategy.putProducer(threadPoolExecutor, consumerContainer, bean, rocketProperties, applicationContext));
		// 销毁临时线程池
		threadPoolExecutor.shutdown();
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
