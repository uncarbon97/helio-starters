package cc.uncarbon.framework.helio.openapi.nextdoc4j;

import cc.uncarbon.framework.helio.base.enums.BaseEnum;
import top.nextdoc4j.enums.resolver.EnumMetadataResolver;

/**
 * 自定义 Nextdoc4j 对于 {@link BaseEnum} 的解析器
 * <a href="https://nextdoc4j.top/guide/plugin/enum.html">官方文档</a>
 *
 * @author Uncarbon
 */
public class BaseEnumResolver implements EnumMetadataResolver {

    @Override
    public boolean supports(Class<?> enumClass) {
        return enumClass != null && enumClass.isEnum() && BaseEnum.class.isAssignableFrom(enumClass);
    }

    @Override
    public Class<?> getEnumInterfaceType() {
        return BaseEnum.class;
    }

    @Override
    public String getValueMethodName() {
        return EnumMetadataResolver.super.getValueMethodName();
    }

    @Override
    public String getDescriptionMethodName() {
        return "getLabel";
    }
}
