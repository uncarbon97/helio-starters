/**
 * Copyright 2021 the original author or authors.
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
package cc.uncabon.framework.rocketmq.aliyun.core.factory;

import cc.uncabon.framework.rocketmq.annotation.ShardingKey;
import cn.hutool.core.util.ObjectUtil;

import java.lang.reflect.Parameter;

/**
 * Classname: ShardingKeyFactory
 * Description:
 * Date: 2021/11/3 20:00
 *
 * @author ThierrySquirrel
 * @since JDK 11
 */
public class ShardingKeyFactory {
    private ShardingKeyFactory() {
    }

    public static String getShardingKeyFactory(Object[] args, Parameter[] params) {
        for (int i = 0; i < args.length; i++) {
            ShardingKey annotation = params[i].getAnnotation(ShardingKey.class);
            if (ObjectUtil.isNotEmpty(annotation)) {
                return (String) args[i];
            }
        }
        return null;
    }
}
