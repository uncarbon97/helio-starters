package cc.uncarbon.framework.helium.bizlog.model;

import cc.uncarbon.framework.helium.bizlog.annotation.LogRecord;
import lombok.Builder;
import lombok.Data;

/**
 * {@link LogRecord} 注解解析后的操作对象
 * <p>
 * 注解各属性的 POJO 形态，后续模板渲染统一从此对象取字段。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Builder
@Data
public class LogRecordOps {
    /**
     * 成功日志模板
     */
    private String successLogTemplate;
    /**
     * 失败日志模板
     */
    private String failLogTemplate;
    /**
     * 主模块，比如：订单、商品
     */
    private String mainModule;
    /**
     * 副模块，比如：创建订单、修改商品
     */
    private String subModule;
    /**
     * 操作人（SpEL，留空则走操作人服务）
     */
    private String operatorId;
    /**
     * 业务标识
     */
    private String bizNo;
    /**
     * 额外信息
     */
    private String extra;
    /**
     * 是否记录的条件
     */
    private String condition;
    /**
     * 自定义成功判定条件
     */
    private String isSuccess;
}
