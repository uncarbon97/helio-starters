package cc.uncarbon.framework.helium.bizlog.support.diff;

import cc.uncarbon.framework.helium.bizlog.props.HeliumBizLogProperties;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

/**
 * diff 文案格式化工具。
 *
 * @author Uncarbon
 */
@UtilityClass
public class DiffTextFormatter {

    /**
     * 字段名称的替换变量
     */
    public static final String FIELD_PLACEHOLDER = "__fieldName";
    /**
     * 更新前的值的替换变量
     */
    public static final String SOURCE_VALUE_PLACEHOLDER = "__sourceValue";
    /**
     * 更新后的值的替换变量
     */
    public static final String TARGET_VALUE_PLACEHOLDER = "__targetValue";
    /**
     * 列表添加项的值的替换变量
     */
    public static final String LIST_ADD_VALUE_PLACEHOLDER = "__addValues";
    /**
     * 列表删除项的值的替换变量
     */
    public static final String LIST_DEL_VALUE_PLACEHOLDER = "__delValues";

    /**
     * 按新增模板格式化文案。
     *
     * @param props       业务日志配置
     * @param fieldName   字段名
     * @param targetValue 目标值
     * @return 格式化后的文案
     */
    public static String formatAdd(HeliumBizLogProperties props, String fieldName, Object targetValue) {
        return props.getAddTemplate().replace(FIELD_PLACEHOLDER, fieldName)
                .replace(TARGET_VALUE_PLACEHOLDER, String.valueOf(targetValue));
    }

    /**
     * 按更新模板格式化文案。
     *
     * @param props       业务日志配置
     * @param fieldName   字段名
     * @param sourceValue 源值
     * @param targetValue 目标值
     * @return 格式化后的文案
     */
    public static String formatUpdate(HeliumBizLogProperties props, String fieldName, Object sourceValue, Object targetValue) {
        return props.getUpdateTemplate().replace(FIELD_PLACEHOLDER, fieldName)
                .replace(SOURCE_VALUE_PLACEHOLDER, String.valueOf(sourceValue))
                .replace(TARGET_VALUE_PLACEHOLDER, String.valueOf(targetValue));
    }

    /**
     * 按删除模板格式化文案。
     *
     * @param props       业务日志配置
     * @param fieldName   字段名
     * @param sourceValue 源值
     * @return 格式化后的文案
     */
    public static String formatDeleted(HeliumBizLogProperties props, String fieldName, Object sourceValue) {
        return props.getDeleteTemplate().replace(FIELD_PLACEHOLDER, fieldName)
                .replace(SOURCE_VALUE_PLACEHOLDER, String.valueOf(sourceValue));
    }

    /**
     * 按列表模板格式化文案，根据添加/删除内容的有无选择对应模板。
     *
     * @param props      业务日志配置
     * @param fieldName  字段名
     * @param addContent 添加项文案
     * @param delContent 删除项文案
     * @return 格式化后的文案
     */
    public static String formatList(HeliumBizLogProperties props, String fieldName, String addContent, String delContent) {
        if (CharSequenceUtil.isNotEmpty(addContent) && CharSequenceUtil.isEmpty(delContent)) {
            return props.getAddTemplateForList()
                    .replace(FIELD_PLACEHOLDER, fieldName)
                    .replace(LIST_ADD_VALUE_PLACEHOLDER, addContent);
        }
        if (CharSequenceUtil.isEmpty(addContent) && CharSequenceUtil.isNotEmpty(delContent)) {
            return props.getDeleteTemplateForList()
                    .replace(FIELD_PLACEHOLDER, fieldName)
                    .replace(LIST_DEL_VALUE_PLACEHOLDER, delContent);
        }
        if (CharSequenceUtil.isNotEmpty(addContent) && CharSequenceUtil.isNotEmpty(delContent)) {
            return props.getUpdateTemplateForList()
                    .replace(FIELD_PLACEHOLDER, fieldName)
                    .replace(LIST_ADD_VALUE_PLACEHOLDER, addContent)
                    .replace(LIST_DEL_VALUE_PLACEHOLDER, delContent);
        }
        return "";
    }
}
