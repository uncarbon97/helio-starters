package cc.uncarbon.framework.redis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum KeyTypeEnum {

    // 基本类型
    STRING,
    HASH,
    LIST,
    SET,
    ZSET,
    STREAM,
    RE_JSON,

    // 封装类型
    LOCK,
    PUB_SUB,
    HYPER_LOG_LOG,
    GEO,

    // 尚未收录的
    OTHER

}
