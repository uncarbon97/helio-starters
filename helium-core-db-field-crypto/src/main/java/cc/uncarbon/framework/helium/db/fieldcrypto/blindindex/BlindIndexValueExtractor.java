package cc.uncarbon.framework.helium.db.fieldcrypto.blindindex;

import cc.uncarbon.framework.helium.db.fieldcrypto.annotation.BlindIndexOf;

/**
 * 盲索引取值提取器
 *
 * <p>由使用方声明在 {@link BlindIndexOf#extractor()}，需具备 public 无参构造
 *
 * @author Uncarbon
 */
@FunctionalInterface
public interface BlindIndexValueExtractor {

    /**
     * @return 规范化后的值；返回 null 表示放弃（对应索引列将被置空）
     */
    String extract(String rawValue);
}
