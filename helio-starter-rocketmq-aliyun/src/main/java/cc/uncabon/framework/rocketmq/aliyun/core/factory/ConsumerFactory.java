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

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.batch.BatchConsumer;
import com.aliyun.openservices.ons.api.order.OrderConsumer;

import java.util.Properties;


/**
 * ClassName: ConsumerFactory
 * Description:
 * date: 2019/4/27 15:55
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class ConsumerFactory {
	private ConsumerFactory() {
	}

	public static Consumer createConsumer(Properties properties) {
		return ONSFactory.createConsumer(properties);
	}


	public static OrderConsumer createOrderConsumer(Properties properties) {
		return ONSFactory.createOrderedConsumer(properties);
	}

	public static BatchConsumer createBatchConsumer(Properties properties) {
		return ONSFactory.createBatchConsumer(properties);
	}
}
