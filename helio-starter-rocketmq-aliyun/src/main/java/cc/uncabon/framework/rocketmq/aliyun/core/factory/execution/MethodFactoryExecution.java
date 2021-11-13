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

package cc.uncabon.framework.rocketmq.aliyun.core.factory.execution;

import cc.uncabon.framework.rocketmq.aliyun.core.factory.MethodFactory;
import cc.uncabon.framework.rocketmq.aliyun.core.serializer.RocketSerializer;
import cc.uncabon.framework.rocketmq.exception.RocketException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * ClassName: MethodFactoryExecution
 * Description:
 * date: 2019/4/27 16:26
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
@Data
@Slf4j
public class MethodFactoryExecution {
    private Object bean;
    private Method method;
    private RocketSerializer rocketSerializer;

    public MethodFactoryExecution(Object bean, Method method, RocketSerializer rocketSerializer) {
        this.bean = bean;
        this.method = method;
        this.rocketSerializer = rocketSerializer;
    }

    public void methodExecution(byte[] message) throws RocketException {
        try {
            Class<?> methodParameter = MethodFactory.getMethodParameter (method);
            Object methodParameterBean = rocketSerializer.deSerialize (message, methodParameter);
            method.invoke (bean, methodParameterBean);
        } catch (Exception e) {
            throw new RocketException (e);
        }
    }
}
