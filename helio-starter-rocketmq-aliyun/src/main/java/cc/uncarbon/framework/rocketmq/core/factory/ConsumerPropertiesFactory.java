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

import cc.uncarbon.framework.rocketmq.annotation.RocketListener;
import cc.uncarbon.framework.rocketmq.props.AliyunRocketProperties;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import java.util.Objects;
import java.util.Properties;

/**
 * ClassName: ConsumerPropertiesFactory
 * Description:
 * date: 2019/4/27 15:37
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public final class ConsumerPropertiesFactory {
	private ConsumerPropertiesFactory() {
	}

	public static Properties createConsumerProperties(AliyunRocketProperties rocketProperties,
													  RocketListener rocketListener) {

		Properties properties = PropertiesFactory.createProperties(rocketProperties);

        properties.put(PropertyKeyConst.GROUP_ID, rocketListener.groupID());
        properties.put(PropertyKeyConst.MessageModel, rocketListener.messageModel());
        properties.put(PropertyKeyConst.ConsumeThreadNums,
				// 消费线程数量，特定需求可以单独指定
                Objects.nonNull(rocketListener.consumeThreadNums()) ? rocketListener.consumeThreadNums()
                        : rocketProperties.getConsumeThreadNums());
        properties.put(PropertyKeyConst.MaxReconsumeTimes, rocketProperties.getMaxReconsumeTimes());
        properties.put(PropertyKeyConst.ConsumeTimeout, rocketProperties.getConsumeTimeout());

		if (Objects.nonNull(rocketListener.consumeBatchSize())) {
			// 一次最大拉取消费数量，特定需求可以单独指定
			properties.put(PropertyKeyConst.MAX_BATCH_MESSAGE_COUNT, rocketListener.consumeBatchSize());
		}

        return properties;

    }
}
