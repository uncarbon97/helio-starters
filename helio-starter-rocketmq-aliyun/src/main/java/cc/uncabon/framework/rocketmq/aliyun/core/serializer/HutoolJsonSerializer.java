package cc.uncabon.framework.rocketmq.aliyun.core.serializer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.nio.charset.StandardCharsets;

/**
 * 基于 Hutool JSON 的 MQ Message 序列化器
 *
 * @author Uncarbon
 */
public class HutoolJsonSerializer implements RocketSerializer {

    @Override
    public <T> byte[] serialize(T object) {
        if (object == null) {
            return new byte[0];
        }

        return JSONUtil.toJsonStr(object).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
        if (bytes.length == 0 || clazz == null) {
            return null;
        }

        return JSONUtil.toBean(StrUtil.toString(bytes), clazz);
    }
}
