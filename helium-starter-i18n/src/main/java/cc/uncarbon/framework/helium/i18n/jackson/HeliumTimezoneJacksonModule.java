package cc.uncarbon.framework.helium.i18n.jackson;

import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;

/**
 * i18n 多时区 Jackson module；经 SPI（{@code META-INF/services/tools.jackson.databind.Module}）
 * 被 {@code JsonMapperFactory.findAndAddModules()} 自动注入，无需手动接线。
 *
 * <p>把 {@link Instant} 序列化为客户端时区 ISO-8601 带偏移字符串（详见 {@link TimezoneAwareInstantSerializer}）；
 * 覆盖 Jackson 内建 JavaTimeModule 对 Instant 的 UTC 默认实现。
 *
 * @author Uncarbon
 */
public class HeliumTimezoneJacksonModule extends SimpleModule {

    public HeliumTimezoneJacksonModule() {
        super(HeliumTimezoneJacksonModule.class.getSimpleName(), Version.unknownVersion());
        this.addSerializer(Instant.class, TimezoneAwareInstantSerializer.INSTANCE);
    }
}
