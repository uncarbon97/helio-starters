package cc.uncarbon.framework.helio.jackson.module;

import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 序列化规则模块：大整数转字符串，避免精度丢失问题
 *
 * @author Uncarbon
 */
public class BigintAsStringModule extends SimpleModule {

    public BigintAsStringModule() {
        super(BigintAsStringModule.class.getSimpleName(), Version.unknownVersion());
        this.addSerializer(Long.class, ToStringSerializer.instance);
        this.addSerializer(Long.TYPE, ToStringSerializer.instance);
        this.addSerializer(BigInteger.class, ToStringSerializer.instance);
        this.addSerializer(BigDecimal.class, ToStringSerializer.instance);
    }
}
