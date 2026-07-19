package cc.uncarbon.framework.helium.bizlog.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.bizlog.support.diff.DiffTextFormatter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * Helium 集成业务日志配置属性类
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.BIZLOG)
@Data
public class HeliumBizLogProperties {

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 命名空间
     */
    private String namespace = "";

    /**
     * 记录日志与业务方法是否使用同一事务；true 时日志异常会回滚业务事务。默认独立。
     */
    private boolean joinTransaction = false;

    /**
     * 日志切面 advisor 的执行顺序，默认最低优先级。
     */
    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * 字段从空改为有值的时候的日志内容模板
     */
    private String addTemplate = "【" + DiffTextFormatter.FIELD_PLACEHOLDER + "】从【空】修改为【" + DiffTextFormatter.TARGET_VALUE_PLACEHOLDER + "】";
    /**
     * 列表修改后只有添加项的时候的日志内容模板
     */
    private String addTemplateForList = "【" + DiffTextFormatter.FIELD_PLACEHOLDER + "】添加了【" + DiffTextFormatter.LIST_ADD_VALUE_PLACEHOLDER + "】";
    /**
     * 列表修改后只有删除项的时候的日志内容模板
     */
    private String deleteTemplateForList = "【" + DiffTextFormatter.FIELD_PLACEHOLDER + "】删除了【" + DiffTextFormatter.LIST_DEL_VALUE_PLACEHOLDER + "】";
    /**
     * 列表修改后既有删除项又有添加项的时候的日志内容模板
     */
    private String updateTemplateForList = "【" + DiffTextFormatter.FIELD_PLACEHOLDER + "】添加了【" + DiffTextFormatter.LIST_ADD_VALUE_PLACEHOLDER + "】删除了【" + DiffTextFormatter.LIST_DEL_VALUE_PLACEHOLDER + "】";
    /**
     * 字段更新后的日志内容模板
     */
    private String updateTemplate = "【" + DiffTextFormatter.FIELD_PLACEHOLDER + "】从【" + DiffTextFormatter.SOURCE_VALUE_PLACEHOLDER + "】修改为【" + DiffTextFormatter.TARGET_VALUE_PLACEHOLDER + "】";
    /**
     * 字段值被设置为 null 后的日志内容模板
     */
    private String deleteTemplate = "删除了【" + DiffTextFormatter.FIELD_PLACEHOLDER + "】：【" + DiffTextFormatter.SOURCE_VALUE_PLACEHOLDER + "】";
    /**
     * 多个字段的日志内容拼接一起的时候的分隔符
     */
    private String fieldSeparator = "；";
    /**
     * 添加或者删除多个列表项的时候，list 中多个项之间的分隔符
     */
    private String listItemSeparator = "，";
    /**
     * 当对象存在嵌套对象的时候，比如 order 里面有个 user，user 分为创建人和更新人，
     * 那么：创建人『的』用户ID，其中『的』就是 ofWord
     */
    private String ofWord = "的";

    /**
     * 是否不校验文案，全部记录日志（新旧对象相同时是否仍落库）
     */
    private Boolean diffLog = false;

    /**
     * 需强制使用 equals 比较的全限定类名（逗号分隔）
     */
    private String useEqualsMethod;

    /**
     * 设置新增模板，并校验占位符。
     *
     * @param template 新增模板
     */
    public void setAddTemplate(String template) {
        validatePlaceHolder(template);
        this.addTemplate = template;
    }

    /**
     * 设置更新模板，并校验占位符。
     *
     * @param template 更新模板
     */
    public void setUpdateTemplate(String template) {
        validatePlaceHolder(template);
        this.updateTemplate = template;
    }

    /**
     * 设置删除模板，并校验占位符。
     *
     * @param template 删除模板
     */
    public void setDeleteTemplate(String template) {
        validatePlaceHolder(template);
        this.deleteTemplate = template;
    }

    /**
     * 校验模板至少包含字段名 / 源值 / 目标值三类占位符之一。
     *
     * @param template 待校验模板
     */
    private void validatePlaceHolder(String template) {
        if (!template.contains(DiffTextFormatter.FIELD_PLACEHOLDER)
                && !template.contains(DiffTextFormatter.SOURCE_VALUE_PLACEHOLDER)
                && !template.contains(DiffTextFormatter.TARGET_VALUE_PLACEHOLDER)) {
            throw new IllegalArgumentException("请检查 logRecord template, 模板需要配置 {{#fieldName}},{{#sourceValue}},{{#targetValue}} 三个变量中的任何一个");
        }
    }

}
