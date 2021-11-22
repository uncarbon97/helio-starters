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

package cc.uncarbon.framework.rocketmq.core.factory;

import cc.uncarbon.framework.rocketmq.annotation.CommonMessage;
import cc.uncarbon.framework.rocketmq.annotation.OrderMessage;
import cc.uncarbon.framework.rocketmq.annotation.RocketMessage;
import cc.uncarbon.framework.rocketmq.annotation.TransactionMessage;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;

import java.util.Map;

/**
 * ClassName: ProducerConsumerFactory
 * Description:
 * date: 2019/5/3 13:45
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class ProducerConsumerFactory {
	private ProducerConsumerFactory() {
	}

	public static String getProducerConsumerKey(RocketMessage rocketMessage, CommonMessage commonMessage) {
		return rocketMessage.groupID() +
				commonMessage.topic() +
				commonMessage.tag();
	}

	public static String getProducerConsumerKey(RocketMessage rocketMessage, OrderMessage orderMessage) {
		return rocketMessage.groupID() +
				orderMessage.topic() +
				orderMessage.tag();
	}

	public static String getProducerConsumerKey(RocketMessage rocketMessage, TransactionMessage transactionMessage) {
		return rocketMessage.groupID() +
				transactionMessage.topic() +
				transactionMessage.tag();
	}

	public static Producer getProducer(Map<String, Object> consumerContainer, RocketMessage rocketMessage, CommonMessage commonMessage){
		String producerConsumerKey = cc.uncarbon.framework.rocketmq.core.factory.ProducerConsumerFactory.getProducerConsumerKey(rocketMessage, commonMessage);
		return (Producer) consumerContainer.get(producerConsumerKey);
	}

	public static OrderProducer getProducer(Map<String, Object> consumerContainer, RocketMessage rocketMessage, OrderMessage orderMessage){
		String producerConsumerKey = cc.uncarbon.framework.rocketmq.core.factory.ProducerConsumerFactory.getProducerConsumerKey(rocketMessage, orderMessage);
		return (OrderProducer) consumerContainer.get(producerConsumerKey);
	}

	public static TransactionProducer getProducer(Map<String, Object> consumerContainer, RocketMessage rocketMessage, TransactionMessage transactionMessage){
		String producerConsumerKey = cc.uncarbon.framework.rocketmq.core.factory.ProducerConsumerFactory.getProducerConsumerKey(rocketMessage, transactionMessage);
		return (TransactionProducer) consumerContainer.get(producerConsumerKey);
	}
}
