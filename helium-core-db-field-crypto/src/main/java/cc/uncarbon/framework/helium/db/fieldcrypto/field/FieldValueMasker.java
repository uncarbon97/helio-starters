package cc.uncarbon.framework.helium.db.fieldcrypto.field;

import cc.uncarbon.framework.helium.db.fieldcrypto.annotation.MaskedOf;

/**
 * 字段值脱敏器 SPI
 *
 * <p>由使用方声明在 {@link MaskedOf#masker()}，需具备 public 无参构造；实现留在业务侧
 *
 * @author Uncarbon
 */
@FunctionalInterface
public interface FieldValueMasker {

    /**
     * @return 脱敏后的值
     */
    String mask(String plainValue);
}
