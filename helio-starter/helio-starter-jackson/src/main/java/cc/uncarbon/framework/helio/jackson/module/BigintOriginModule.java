package cc.uncarbon.framework.helio.jackson.module;

import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.jdk.NumberSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 序列化规则模块：大整数还原为数值形式展示
 *
 * @author Uncarbon
 **/
public class BigintOriginModule extends SimpleModule {

    public BigintOriginModule() {
        super(BigintOriginModule.class.getSimpleName(), Version.unknownVersion());
        this.addSerializer(Long.class, NumberSerializer.instance);
        this.addSerializer(Long.TYPE, NumberSerializer.instance);
        this.addSerializer(BigInteger.class, NumberSerializer.instance);
        this.addSerializer(BigDecimal.class, NumberSerializer.instance);
    }
}
