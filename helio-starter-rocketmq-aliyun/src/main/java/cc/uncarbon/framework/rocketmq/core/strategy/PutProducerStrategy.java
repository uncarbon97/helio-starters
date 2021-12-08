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
import cc.uncarbon.framework.rocketmq.annotation.OrderMessage;
import cc.uncarbon.framework.rocketmq.annotation.RocketMessage;
import cc.uncarbon.framework.rocketmq.annotation.TransactionMessage;
import cc.uncarbon.framework.rocketmq.core.factory.ProducerConsumerFactory;
import cc.uncarbon.framework.rocketmq.core.factory.ProducerFactory;
import cc.uncarbon.framework.rocketmq.core.factory.ThreadPoolFactory;
import cc.uncarbon.framework.rocketmq.core.utils.ApplicationContextUtils;
import cc.uncarbon.framework.rocketmq.props.AliyunRocketProperties;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ClassName: PutProducerStrategy
 * Description:
 * date: 2019/5/3 18:37
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class PutProducerStrategy {
    private PutProducerStrategy() {
    }

    public static void putProducer(Map<String, Object> producerConsumer, RocketMessage rocketMessage, Object bean, AliyunRocketProperties rocketProperties, ApplicationContext applicationContext) {
        if (bean instanceof CommonMessage) {
            CommonMessage commonMessage = (CommonMessage) bean;
            String producerConsumerKey = ProducerConsumerFactory.getProducerConsumerKey(rocketMessage, commonMessage);
            Producer producer = ProducerFactory.createProducer(rocketMessage, rocketProperties);
            ThreadPoolExecutor callbackThreadPoolExecutor = ThreadPoolFactory.createCallbackThreadPoolExecutor(rocketProperties);
            producer.start();
            producer.setCallbackExecutor(callbackThreadPoolExecutor);
            producerConsumer.put(producerConsumerKey, producer);
            return;
        }
        if (bean instanceof OrderMessage) {
            OrderMessage orderMessage = (OrderMessage) bean;
            String producerConsumerKey = ProducerConsumerFactory.getProducerConsumerKey(rocketMessage, orderMessage);
            OrderProducer orderProducer = ProducerFactory.createOrderProducer(rocketMessage, rocketProperties);
            orderProducer.start();
            producerConsumer.put(producerConsumerKey, orderProducer);
            return;
        }
        if (bean instanceof TransactionMessage) {
            TransactionMessage transactionMessage = (TransactionMessage) bean;
            String producerConsumerKey = ProducerConsumerFactory.getProducerConsumerKey(rocketMessage, transactionMessage);
            LocalTransactionChecker localTransactionChecker = ApplicationContextUtils.getLocalTransactionChecker(applicationContext, transactionMessage.transactionStatus(), transactionMessage.checker());
            TransactionProducer transactionProducer = ProducerFactory.createTransactionProducer(rocketMessage, rocketProperties, localTransactionChecker);
            transactionProducer.start();
            producerConsumer.put(producerConsumerKey, transactionProducer);
        }
    }
}
