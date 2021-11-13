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

package cc.uncabon.framework.rocketmq.aliyun.core.consumer;

import cc.uncabon.framework.rocketmq.aliyun.core.factory.execution.MethodFactoryExecution;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: AbstractRocketListener
 * Description:
 * date: 2019/4/27 17:07
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Data
@Slf4j
public abstract class AbstractRocketListener {
	private MethodFactoryExecution methodFactoryExecution;

	protected AbstractRocketListener(MethodFactoryExecution methodFactoryExecution) {
		this.methodFactoryExecution = methodFactoryExecution;
	}

	public void printErrorLog() {
		log.error("Consumer agent failed！bean:{},method:{}",
				methodFactoryExecution.getBean(),
				methodFactoryExecution.getMethod());
	}


}
