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

package cc.uncarbon.framework.rocketmq.core.producer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: DefaultLocalTransactionExecuter
 * Description:
 * date: 2019/4/28 22:28
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Slf4j
public class DefaultLocalTransactionExecutor implements LocalTransactionExecuter {
	/**
	 * 执行本地事务，由应用来重写
	 *
	 * @param msg 消息
	 * @param arg 应用自定义参数，由send方法传入并回调
	 * @return {@link TransactionStatus} 返回事务执行结果，包括提交事务、回滚事务、未知状态
	 */
	@Override
	public TransactionStatus execute(Message msg, Object arg) {
		log.info(">>>> Execute local transaction message:{}>>>>", msg);
		return TransactionStatus.CommitTransaction;
	}
}
