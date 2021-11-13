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

package cc.uncabon.framework.rocketmq.aliyun.core.factory;

import cc.uncabon.framework.rocketmq.aliyun.core.strategy.SendMessageStrategy;
import cc.uncabon.framework.rocketmq.aliyun.core.utils.ApplicationContextUtils;
import cc.uncabon.framework.rocketmq.annotation.CommonMessage;
import cc.uncabon.framework.rocketmq.annotation.OrderMessage;
import cc.uncabon.framework.rocketmq.annotation.TransactionMessage;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;
import org.springframework.context.ApplicationContext;


/**
 * ClassName: SendMessageFactory
 * Description:
 * date: 2019/4/29 21:52
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class SendMessageFactory {
    private SendMessageFactory() {
    }

    public static void sendMessage(Long startDeliverTime, Producer producer, CommonMessage commonMessage, byte[] bytes, ApplicationContext applicationContext) {
        Message message = MessageFactory.createMessage (commonMessage, bytes);
        if (null != startDeliverTime) {
            message.setStartDeliverTime (startDeliverTime);
        }
        SendMessageStrategy.send (commonMessage, producer, message, applicationContext);

    }

    public static void sendMessage(OrderProducer orderProducer, OrderMessage orderMessage, byte[] bytes, String shardingKeyFactory) {
        Message message = MessageFactory.createMessage (orderMessage, bytes);
        orderProducer.send (message, shardingKeyFactory);
    }

    public static void sendMessage(TransactionProducer transactionProducer, TransactionMessage transactionMessage, byte[] bytes, ApplicationContext applicationContext) {
        Message message = MessageFactory.createMessage (transactionMessage, bytes);
        transactionProducer.send (message, ApplicationContextUtils.getLocalTransactionExecuter (applicationContext, transactionMessage.executer ()), null);
    }
}
