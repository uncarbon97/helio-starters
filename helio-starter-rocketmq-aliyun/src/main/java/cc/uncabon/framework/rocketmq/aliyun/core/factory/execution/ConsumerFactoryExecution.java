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

package cc.uncabon.framework.rocketmq.aliyun.core.factory.execution;

import cc.uncabon.framework.rocketmq.aliyun.core.consumer.DefaultBatchMessageListener;
import cc.uncabon.framework.rocketmq.aliyun.core.consumer.DefaultMessageListener;
import cc.uncabon.framework.rocketmq.aliyun.core.consumer.DefaultMessageOrderListener;
import cc.uncabon.framework.rocketmq.aliyun.core.factory.ConsumerFactory;
import cc.uncabon.framework.rocketmq.aliyun.core.factory.ConsumerPropertiesFactory;
import cc.uncabon.framework.rocketmq.aliyun.thread.AbstractConsumerThread;
import cc.uncabon.framework.rocketmq.annotation.MessageListener;
import cc.uncabon.framework.rocketmq.annotation.RocketListener;
import cc.uncarbon.framework.core.props.AliyunRocketProperties;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.batch.BatchConsumer;
import com.aliyun.openservices.ons.api.order.OrderConsumer;

import java.util.Properties;

/**
 * ClassName: ConsumerFactoryExecution
 * Description:
 * date: 2019/4/27 16:05
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class ConsumerFactoryExecution extends AbstractConsumerThread {


    public ConsumerFactoryExecution(AliyunRocketProperties rocketProperties, RocketListener rocketListener, MessageListener consumerListener, MethodFactoryExecution methodFactoryExecution) {
        super (rocketProperties, rocketListener, consumerListener, methodFactoryExecution);
    }

    @Override
    public void statsConsumer(AliyunRocketProperties rocketProperties,
                              RocketListener rocketListener,
                              MessageListener consumerListener,
                              MethodFactoryExecution methodFactoryExecution) {

        Properties properties = ConsumerPropertiesFactory.createConsumerProperties (rocketProperties, rocketListener);
        if (consumerListener.orderConsumer ()) {
            properties.put (PropertyKeyConst.SuspendTimeMillis, rocketProperties.getSuspendTimeMilli ());
            OrderConsumer orderConsumer = ConsumerFactory.createOrderConsumer (properties);
            orderConsumer.subscribe (consumerListener.topic (), consumerListener.tag (), new DefaultMessageOrderListener (methodFactoryExecution));
            orderConsumer.start ();
            return;
        }
        if (consumerListener.batchConsumer ()) {
            properties.put (PropertyKeyConst.ConsumeMessageBatchMaxSize, consumerListener.consumeMessageBatchMaxSize ());
            properties.put (PropertyKeyConst.BatchConsumeMaxAwaitDurationInSeconds, consumerListener.batchConsumeMaxAwaitDurationInSeconds ());
            BatchConsumer batchConsumer = ConsumerFactory.createBatchConsumer (properties);
            batchConsumer.subscribe (consumerListener.topic (), consumerListener.tag (), new DefaultBatchMessageListener (methodFactoryExecution));
            batchConsumer.start ();
            return;
        }

        Consumer consumer = ConsumerFactory.createConsumer (properties);
        consumer.subscribe (consumerListener.topic (), consumerListener.tag (), new DefaultMessageListener (methodFactoryExecution));
        consumer.start ();
    }
}
