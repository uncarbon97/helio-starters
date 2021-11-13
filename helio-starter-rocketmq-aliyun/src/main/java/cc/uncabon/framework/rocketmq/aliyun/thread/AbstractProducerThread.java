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

package cc.uncabon.framework.rocketmq.aliyun.thread;

import cc.uncabon.framework.rocketmq.annotation.RocketMessage;
import cc.uncarbon.framework.core.props.AliyunRocketProperties;
import lombok.Data;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * ClassName: AbstractProducerThread
 * Description:
 * date: 2019/5/3 14:02
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Data
public abstract class AbstractProducerThread implements Runnable {
	private Map<String, Object> producerConsumer;
	private RocketMessage rocketMessage;
	private Object bean;
	private AliyunRocketProperties rocketProperties;
	private ApplicationContext applicationContext;

	protected AbstractProducerThread(Map<String, Object> producerConsumer, RocketMessage rocketMessage, Object bean, AliyunRocketProperties rocketProperties, ApplicationContext applicationContext) {
		this.producerConsumer = producerConsumer;
		this.rocketMessage = rocketMessage;
		this.bean = bean;
		this.rocketProperties = rocketProperties;
		this.applicationContext = applicationContext;
	}

	/**
	 * 开始向容器装填
	 *
	 * @param producerConsumer   producerConsumer
	 * @param rocketMessage      rocketMessage
	 * @param bean               bean
	 * @param rocketProperties   rocketProperties
	 * @param applicationContext applicationContext
	 */
	protected abstract void statsPutProducer(Map<String, Object> producerConsumer, RocketMessage rocketMessage, Object bean, AliyunRocketProperties rocketProperties, ApplicationContext applicationContext);

	@Override
	public void run() {
		statsPutProducer(producerConsumer, rocketMessage, bean, rocketProperties, applicationContext);
	}
}
