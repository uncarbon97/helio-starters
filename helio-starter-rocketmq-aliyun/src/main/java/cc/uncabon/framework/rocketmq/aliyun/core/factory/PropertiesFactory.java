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

import cc.uncarbon.framework.core.props.AliyunRocketProperties;
import com.aliyun.openservices.ons.api.PropertyKeyConst;

import java.util.Properties;

/**
 * ClassName: PropertiesFactory
 * Description:
 * date: 2019/4/27 20:26
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class PropertiesFactory {
	private PropertiesFactory() {
	}

	public static Properties createProperties(AliyunRocketProperties rocketProperties) {
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.NAMESRV_ADDR, rocketProperties.getNameSrvAddr());
		properties.put(PropertyKeyConst.AccessKey, rocketProperties.getAccessKey());
		properties.put(PropertyKeyConst.SecretKey, rocketProperties.getSecretKey());
		properties.put(PropertyKeyConst.OnsChannel, rocketProperties.getOnsChannel());

		return properties;
	}
}
