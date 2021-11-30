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

package cc.uncarbon.framework.rocketmq.core.factory;

import java.lang.reflect.Method;

/**
 * ClassName: MethodFactory
 * Description:
 * date: 2019/4/27 16:13
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */

public class MethodFactory {
	private MethodFactory() {
	}

	public static Class<?> getMethodParameter(Method method) {
		return method.getParameterTypes()[0];
	}
}