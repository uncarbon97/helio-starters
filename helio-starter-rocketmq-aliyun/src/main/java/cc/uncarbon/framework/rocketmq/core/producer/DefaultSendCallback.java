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

import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: DefaultSendCallback
 * Description:
 * date: 2019/4/29 23:32
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Slf4j
public class DefaultSendCallback implements SendCallback {
    /**
     * 发送成功回调的方法.
     *
     * @param sendResult 发送结果
     */
    @Override
    public void onSuccess(SendResult sendResult) {
        log.info("Message sent successfully. >> sendResult = {}", sendResult);
    }

    /**
     * 发送失败回调方法.
     *
     * @param context 失败上下文.
     */
    @Override
    public void onException(OnExceptionContext context) {
        log.error("Failed to send message. >> topic = {}, messageId = {}, exception ", context.getTopic(), context.getMessageId(), context.getException());
    }
}
