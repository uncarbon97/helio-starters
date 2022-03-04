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

package cc.uncarbon.framework.rocketmq.annotation;

import com.aliyun.openservices.ons.api.PropertyValueConst;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * ClassName: RocketListener
 * Description:
 * date: 2019/4/26 21:35
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface RocketListener {
	/**
	 * 您在控制台创建的 Group ID
	 *
	 * @return String
	 */
	String groupID() default "";

	/**
	 * 消费模式，默认集群消费
	 *
	 * @return String
	 */
	String messageModel() default PropertyValueConst.CLUSTERING;

	/**
	 * 消费线程数量，特定需求可以单独指定，默认同配置文件
	 *
	 * @return String
	 */
	String consumeThreadNums();

	/**
	 * 一次最大拉取消费数量，特定需求可以单独指定，默认同配置文件
	 *
	 * @return String
	 */
	String consumeBatchSize();

}
