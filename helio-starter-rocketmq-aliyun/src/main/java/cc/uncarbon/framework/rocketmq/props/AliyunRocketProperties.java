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

package cc.uncarbon.framework.rocketmq.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * 阿里云 Rocket MQ 配置项
 *
 * @author ThierrySquirrel
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = "helio.rocketmq.aliyun", ignoreInvalidFields = true)
@Data
public class AliyunRocketProperties {

	/**
	 * 设置 TCP 协议接入点，从控制台获取
	 */
	private String nameSrvAddr;

	/**
	 * 您在阿里云账号管理控制台中创建的 AccessKey，用于身份认证
	 */
	private String accessKey;

	/**
	 * 您在阿里云账号管理控制台中创建的 SecretKey，用于身份认证
	 */
	private String secretKey;

	/**
	 * 用户渠道，默认为：ALIYUN，聚石塔用户为：CLOUD
	 */
	private String onsChannel = "ALIYUN";

	/**
	 * 设置消息发送的超时时间，单位（毫秒），默认：3000
	 */
	private Integer sendMsgTimeoutMillis = 3000;

	/**
	 * 设置事务消息第一次回查的最快时间，单位（秒）
	 */
	private Integer checkImmunityTimeInSeconds = 5;

	/**
	 * 设置 RocketMessage 实例的消费线程数，阿里云默认：20
	 * 默认cpu数量*2+1
	 */
	private Integer consumeThreadNums = Runtime.getRuntime().availableProcessors() * 2 + 1;

	/**
	 * 设置消息消费失败的最大重试次数，默认：16
	 */
	private Integer maxReconsumeTimes = 16;

	/**
	 * 设置每条消息消费的最大超时时间，超过设置时间则被视为消费失败，等下次重新投递再次消费。 每个业务需要设置一个合理的值，单位（分钟）。 默认：15
	 */
	private Integer consumeTimeout = 15;

	/**
	 * 只适用于顺序消息，设置消息消费失败的重试间隔时间默认100毫秒
	 */
	private Integer suspendTimeMilli = 100;

	/**
	 * 异步发送消息执行Callback的目标线程池
	 */
	private Integer callbackThreadNums = Runtime.getRuntime().availableProcessors() * 2 + 1;

	/**
	 * 初始化消费者线程数，（尽量和消费者数量一致）默认cpu数量*2+1
	 */
	private Integer createConsumeThreadNums = Runtime.getRuntime().availableProcessors() * 2 + 1;

	/**
	 * 初始化生产者线程数，（尽量和生产者数量一致）默认cpu数量*2+1
	 */
	private Integer createProducerThreadNums = Runtime.getRuntime().availableProcessors() * 2 + 1;

	/**
	 * 生产者发送消息线程数 默认cpu数量*2+1
	 */
	private Integer sendMessageThreadNums = Runtime.getRuntime().availableProcessors() * 2 + 1;

}
