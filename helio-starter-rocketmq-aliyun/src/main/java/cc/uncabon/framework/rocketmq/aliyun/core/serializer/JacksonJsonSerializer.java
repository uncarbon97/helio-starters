package cc.uncabon.framework.rocketmq.aliyun.core.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 基于 Jackson JSON 的 MQ Message 序列化器
 *
 * @author Uncarbon
 */
@Slf4j
public class JacksonJsonSerializer implements RocketSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) {
        if (object != null) {
            try {
                return objectMapper.writeValueAsBytes(object);
            } catch (JsonProcessingException jpe) {
                log.error("[Rocket MQ-Aliyun][JacksonJsonSerializer] 序列化时发生异常 >> 堆栈", jpe);
            }
        }

        return new byte[0];
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
        if (bytes.length > 0 && clazz != null) {
            try {
                return objectMapper.readValue(bytes, clazz);
            } catch (IOException e) {
                log.error("[Rocket MQ-Aliyun][JacksonJsonSerializer] 反序列化时发生异常 >> 堆栈", e);
            }
        }

        return null;
    }
}
