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

package cc.uncabon.framework.rocketmq.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * ClassName: MessageListener
 * Description:
 * date: 2019/4/26 22:37
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface MessageListener {
	/**
	 * Message 所属的 Topic
	 *
	 * @return String
	 */
	String topic() default "";

	/**
	 * 订阅指定 Topic 下的 Tags：
	 * 1. * 表示订阅所有消息
	 * 2. TagA || TagB || TagC 表示订阅 TagA 或 TagB 或 TagC 的消息
	 *
	 * @return String
	 */
	String tag() default "*";

	/**
	 * 是否为顺序消息
	 *
	 * @return Boolean
	 */

	boolean orderConsumer() default false;

	/**
	 * 是否为批量消息
	 *
	 * @return Boolean
	 */
	boolean batchConsumer() default false;

	/**
	 * 设置批量消费最大消息数量,当指定Topic的消息数量已经攒够128条,SDK立即执行回调进行消费.默认值：32,取值范围：1~1024.
	 */
	int consumeMessageBatchMaxSize() default 32;

	/**
	 * 设置批量消费最大等待时长,当等待时间达到10秒,SDK立即执行回调进行消费.默认值：0,取值范围：0~450,单位：秒.
	 */
	int batchConsumeMaxAwaitDurationInSeconds() default 0;
}
