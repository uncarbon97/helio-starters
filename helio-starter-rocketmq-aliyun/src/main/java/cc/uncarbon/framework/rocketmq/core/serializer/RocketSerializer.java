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

package cc.uncarbon.framework.rocketmq.core.serializer;

/**
 * ClassName: RocketSerializer
 * Description:
 * date: 2019/10/17 18:24
 *
 * @author ThierrySquirrel
 * @since JDK 1.8
 */
public interface RocketSerializer {
	/**
	 * 序列化为byte[]
	 * @param object 对象
	 * @param <T> 泛型
	 * @return 二进制数据
	 */
	<T> byte[] serialize(T object);

	/**
	 * byte[]反序列化为对象
	 *
	 * @param bytes 序列化的二进制数据
	 * @param clazz 反序列化后的对象
	 * @param <T>   T
	 * @return 对象
	 */
	<T> T deSerialize(byte[] bytes, Class<T> clazz);
}
