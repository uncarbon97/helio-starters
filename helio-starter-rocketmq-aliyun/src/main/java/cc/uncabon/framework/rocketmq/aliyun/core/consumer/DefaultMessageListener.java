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
import cc.uncabon.framework.rocketmq.exception.RocketException;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: DefaultMessageListener
 * Description:
 * date: 2019/4/27 17:05
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Slf4j
public class DefaultMessageListener extends AbstractRocketListener implements MessageListener {

    public DefaultMessageListener(MethodFactoryExecution methodFactoryExecution) {
        super (methodFactoryExecution);
    }

    /**
     * 消费消息接口，由应用来实现<br>
     * 网络抖动等不稳定的情形可能会带来消息重复，对重复消息敏感的业务可对消息做幂等处理
     *
     * @param message 消息
     * @param context 消费上下文
     * @return 消费结果，如果应用抛出异常或者返回Null等价于返回Action.ReconsumeLater
     * @see <a href="https://help.aliyun.com/document_detail/44397.html">如何做到消费幂等</a>
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        log.info (">>>>message:{}>>>>", message);
        try {
            super.getMethodFactoryExecution ().methodExecution (message.getBody ());
        } catch (RocketException e) {
            super.printErrorLog ();
            return Action.ReconsumeLater;
        }
        return Action.CommitMessage;
    }
}
