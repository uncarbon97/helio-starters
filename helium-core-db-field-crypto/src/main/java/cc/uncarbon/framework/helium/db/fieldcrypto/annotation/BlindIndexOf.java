package cc.uncarbon.framework.helium.db.fieldcrypto.annotation;

import cc.uncarbon.framework.helium.db.fieldcrypto.blindindex.BlindIndexValueExtractor;
import cc.uncarbon.framework.helium.db.fieldcrypto.blindindex.BlindIndexValueExtractors;

import java.lang.annotation.*;

/**
 * 标注在实体的「盲索引字段」上，声明其派生来源
 *
 * @author Uncarbon
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BlindIndexOf {

    /**
     * 敏感字段名
     */
    String value();

    /**
     * index-key 派生用途标识（如 sys_user.phone_no.full）
     * <P>字符集 [a-z0-9_.-]{3,128}
     */
    String purpose();

    /**
     * 取值提取器，需具备 public 无参构造；或具备 public {@code (int)} 构造并配合 {@link #extractorArg()}
     */
    Class<? extends BlindIndexValueExtractor> extractor() default BlindIndexValueExtractors.Trim.class;

    /**
     * 提取器整型参数（如 {@link BlindIndexValueExtractors.TailDigits} 的尾部位数）；
     * 为负数表示不传参、走无参构造
     */
    int extractorArg() default -1;
}
