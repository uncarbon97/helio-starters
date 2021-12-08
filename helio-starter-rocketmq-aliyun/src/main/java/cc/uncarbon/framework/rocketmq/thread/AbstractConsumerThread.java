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

package cc.uncarbon.framework.rocketmq.thread;

import cc.uncarbon.framework.rocketmq.annotation.MessageListener;
import cc.uncarbon.framework.rocketmq.annotation.RocketListener;
import cc.uncarbon.framework.rocketmq.core.factory.execution.MethodFactoryExecution;
import cc.uncarbon.framework.rocketmq.props.AliyunRocketProperties;
import lombok.Data;

/**
 * ClassName: AbstractConsumerThread
 * Description:
 * date: 2019/4/27 20:03
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Data
public abstract class AbstractConsumerThread implements Runnable {
	private AliyunRocketProperties rocketProperties;
	private RocketListener rocketListener;
	private MessageListener consumerListener;
	private MethodFactoryExecution methodFactoryExecution;

	protected AbstractConsumerThread(AliyunRocketProperties rocketProperties, RocketListener rocketListener, MessageListener consumerListener, MethodFactoryExecution methodFactoryExecution) {
		this.rocketProperties = rocketProperties;
		this.rocketListener = rocketListener;
		this.consumerListener = consumerListener;
		this.methodFactoryExecution = methodFactoryExecution;
	}

	/**
	 * 消费者开始监听
	 *
	 * @param rocketProperties       rocketProperties
	 * @param rocketListener         rocketListener
	 * @param consumerListener       consumerListener
	 * @param methodFactoryExecution methodFactoryExecution
	 */
	protected abstract void statsConsumer(AliyunRocketProperties rocketProperties,
										  RocketListener rocketListener,
										  MessageListener consumerListener,
										  MethodFactoryExecution methodFactoryExecution);

	@Override
	public void run() {
		statsConsumer(this.getRocketProperties(),
				this.getRocketListener(),
				this.getConsumerListener(),
				this.getMethodFactoryExecution());
	}
}
