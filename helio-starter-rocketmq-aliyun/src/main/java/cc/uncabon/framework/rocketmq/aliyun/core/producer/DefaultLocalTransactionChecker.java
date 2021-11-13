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

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: DefaultLocalTransactionChecker
 * Description:
 * date: 2019/4/28 21:42
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Data
@Slf4j
public class DefaultLocalTransactionChecker implements LocalTransactionChecker {
	private TransactionStatus transactionStatus;

	public DefaultLocalTransactionChecker(TransactionStatus transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	/**
	 * 回查本地事务，Broker回调Producer，将未结束的事务发给Producer，由Producer来再次决定事务是提交还是回滚
	 *
	 * @param msg 消息
	 * @return {@link TransactionStatus} 事务状态, 包含提交事务、回滚事务、未知状态
	 */
	@Override
	public TransactionStatus check(Message msg) {
		log.info(">>>> Review local transactions message:{}>>>>", msg);
		return transactionStatus;
	}
}
