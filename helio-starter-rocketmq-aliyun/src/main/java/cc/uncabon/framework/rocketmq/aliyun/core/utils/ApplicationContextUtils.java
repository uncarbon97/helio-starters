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

package cc.uncabon.framework.rocketmq.aliyun.core.utils;

import cc.uncabon.framework.rocketmq.aliyun.core.producer.DefaultLocalTransactionChecker;
import cc.uncabon.framework.rocketmq.aliyun.core.producer.DefaultLocalTransactionExecuter;
import cc.uncabon.framework.rocketmq.aliyun.core.producer.DefaultSendCallback;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import org.springframework.context.ApplicationContext;

/**
 * ClassName: ApplicationContextUtils
 * Description:
 * date: 2019/7/11 18:47
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class ApplicationContextUtils {
	private ApplicationContextUtils() {
	}

	public static SendCallback getSendCallback(ApplicationContext applicationContext, Class<? extends SendCallback> callback) {
		SendCallback sendCallback;
		if (DefaultSendCallback.class.equals(callback)) {
			sendCallback = new DefaultSendCallback();
		} else {
			sendCallback = applicationContext.getBean(callback);
		}
		return sendCallback;
	}

	public static LocalTransactionChecker getLocalTransactionChecker(ApplicationContext applicationContext, TransactionStatus transactionStatus, Class<? extends LocalTransactionChecker> checker) {
		LocalTransactionChecker localTransactionChecker;
		if (DefaultLocalTransactionChecker.class.equals(checker)) {
			localTransactionChecker = new DefaultLocalTransactionChecker(transactionStatus);
		} else {
			localTransactionChecker = applicationContext.getBean(checker);
		}
		return localTransactionChecker;
	}

	public static LocalTransactionExecuter getLocalTransactionExecuter(ApplicationContext applicationContext, Class<? extends LocalTransactionExecuter> executer) {
		LocalTransactionExecuter localTransactionExecuter;
		if (DefaultLocalTransactionExecuter.class.equals(executer)) {
			localTransactionExecuter = new DefaultLocalTransactionExecuter();
		} else {
			localTransactionExecuter = applicationContext.getBean(executer);
		}
		return localTransactionExecuter;
	}
}
