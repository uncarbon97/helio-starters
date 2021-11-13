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

package cc.uncabon.framework.rocketmq.aliyun.core.producer;

/**
 * ClassName: MessageSendType
 * Description:
 * date: 2019/4/28 21:02
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public enum MessageSendType {
	/**
	 * 同步发送
	 */
	SEND,
	/**
	 * 异步发送
	 */
	SEND_ASYNC,
	/**
	 * 单向发送
	 */
	SEND_ONE_WAY

}
