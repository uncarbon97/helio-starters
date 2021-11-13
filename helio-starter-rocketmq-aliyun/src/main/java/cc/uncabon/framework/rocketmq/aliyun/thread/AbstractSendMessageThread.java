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

package cc.uncabon.framework.rocketmq.aliyun.thread;

import cc.uncabon.framework.rocketmq.annotation.RocketMessage;
import lombok.Data;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * ClassName: AbstractSendMessageThread
 * Description:
 * date: 2019/4/29 21:39
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Data
public abstract class AbstractSendMessageThread implements Runnable {
    private Long startDeliverTime;
    private String shardingKeyFactory;
    private Map<String, Object> consumerContainer;
    private RocketMessage rocketMessage;
    private Object message;
    private byte[] bytes;
    private ApplicationContext applicationContext;

    protected AbstractSendMessageThread(Long startDeliverTime, String shardingKeyFactory, Map<String, Object> consumerContainer, RocketMessage rocketMessage, Object message, byte[] bytes, ApplicationContext applicationContext) {
        this.startDeliverTime = startDeliverTime;
        this.shardingKeyFactory = shardingKeyFactory;
        this.consumerContainer = consumerContainer;
        this.rocketMessage = rocketMessage;
        this.message = message;
        this.bytes = bytes;
        this.applicationContext = applicationContext;
    }

    /**
     * 开始发送消息
     *
     * @param startDeliverTime   startDeliverTime
     * @param shardingKeyFactory shardingKeyFactory
     * @param consumerContainer  consumerContainer
     * @param rocketMessage      rocketMessage
     * @param message            message
     * @param bytes              bytes
     * @param applicationContext applicationContext
     */
    protected abstract void statsSendMessage(Long startDeliverTime, String shardingKeyFactory, Map<String, Object> consumerContainer, RocketMessage rocketMessage, Object message, byte[] bytes, ApplicationContext applicationContext);

    @Override
    public void run() {
        statsSendMessage (startDeliverTime, shardingKeyFactory, consumerContainer,
                rocketMessage,
                message,
                bytes,
                applicationContext);
    }
}
