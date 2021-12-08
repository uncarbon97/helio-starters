package cc.uncarbon.framework.rocketmq.core.serializer;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONUtil;

import java.nio.charset.StandardCharsets;

/**
 * 基于 Hutool JSON 的 MQ Message 序列化器
 *
 * @author Uncarbon
 */
public class Base64Serializer implements RocketSerializer {

    @Override
    public <T> byte[] serialize(T object) {
        if (object == null) {
            return new byte[0];
        }

        return Base64.encode(JSONUtil.toJsonStr(object)).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
        if (bytes.length == 0 || clazz == null) {
            return null;
        }

        return JSONUtil.toBean(new String(Base64.decode(bytes)), clazz);
    }
}
