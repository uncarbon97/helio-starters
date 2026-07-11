package cc.uncarbon.framework.helium.i18n.jackson;

import cc.uncarbon.framework.helium.i18n.model.Money;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonDeserializer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.JsonSerializer;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;

import java.io.IOException;

/**
 * 杰克逊序列化模块：{@link Money} 原存原取
 *
 * <p>序列化为 {@code {"amount":"12.345678","currency":"USDT"}}（amount 用字符串以保精度）；
 * 反序列化时按 amount 的小数位推断 scale，从而与序列化端往返一致。
 *
 * <p>通过 Jackson SPI（{@code findAndAddModules}）自动发现，不需要声明为 Spring Bean。
 *
 * @author Uncarbon
 */
public class MoneyJacksonModule extends SimpleModule {

    public MoneyJacksonModule() {
        super(MoneyJacksonModule.class.getSimpleName(), Version.unknownVersion());
        this.addSerializer(Money.class, new MoneySerializer());
        this.addDeserializer(Money.class, new MoneyDeserializer());
    }

    public static class MoneySerializer extends JsonSerializer<Money> {
        @Override
        public void serialize(Money value, JsonGenerator g, SerializationContext ctx) throws IOException {
            g.writeStartObject();
            g.writeStringField("amount", value.amount().toPlainString());
            g.writeStringField("currency", value.currencyCode());
            g.writeEndObject();
        }
    }

    public static class MoneyDeserializer extends JsonDeserializer<Money> {
        @Override
        public Money deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            JsonNode node = (JsonNode) p.readValueAsTree();
            String amount = node.get("amount").asText();
            String currency = node.get("currency").asText();
            int scale = fractionDigits(amount);
            return Money.of(amount, currency, scale);
        }

        private static int fractionDigits(String amount) {
            int dot = amount.indexOf('.');
            if (dot < 0) {
                return 0;
            }
            return amount.length() - dot - 1;
        }
    }
}
