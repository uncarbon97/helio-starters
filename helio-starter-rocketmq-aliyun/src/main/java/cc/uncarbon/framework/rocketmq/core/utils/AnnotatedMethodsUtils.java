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

package cc.uncarbon.framework.rocketmq.core.utils;

import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * ClassName: AnnotatedMethodsUtils
 * Description:
 * date: 2019/5/3 11:44
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public class AnnotatedMethodsUtils {
	private AnnotatedMethodsUtils() {
	}

	public static <T extends Annotation> Map<Method, T> getMethodAndAnnotation(Object bean, Class<T> annotation) {
		return MethodIntrospector.selectMethods(bean.getClass(),
				(MethodIntrospector.MetadataLookup<T>) method -> AnnotatedElementUtils
						.findMergedAnnotation(method, annotation));
	}
}
