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

package cc.uncarbon.framework.rocketmq.core.strategy;

import cc.uncarbon.framework.rocketmq.annotation.CommonMessage;
import cc.uncarbon.framework.rocketmq.core.producer.MessageSendType;
import cc.uncarbon.framework.rocketmq.core.utils.ApplicationContextUtils;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import org.springframework.context.ApplicationContext;


/**
 * ClassName: SendMessageStrategy
 * Description:
 * date: 2019/4/29 23:37
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class SendMessageStrategy {
	private SendMessageStrategy() {
	}

	public static void send(CommonMessage commonMessage, Producer producer, Message message, ApplicationContext applicationContext) {
		if (commonMessage.messageSendType().equals(MessageSendType.SEND)) {
			producer.send(message);
			return;
		}
		if (commonMessage.messageSendType().equals(MessageSendType.SEND_ASYNC)) {
			producer.sendAsync(message, ApplicationContextUtils.getSendCallback(applicationContext, commonMessage.callback()));
			return;
		}
		if (commonMessage.messageSendType().equals(MessageSendType.SEND_ONE_WAY)) {
			producer.sendOneway(message);
		}
	}
}
