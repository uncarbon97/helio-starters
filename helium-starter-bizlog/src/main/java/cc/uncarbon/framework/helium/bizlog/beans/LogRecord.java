package cc.uncarbon.framework.helium.bizlog.beans;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 业务日志实体
 * <p>
 * 一条业务操作日志的完整数据，由 {@code LogRecordInterceptor} 渲染文案后交由
 * {@code ILogRecordService} 落库。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LogRecord {
    /**
     * 主键 id
     */
    private Serializable id;
    /**
     * 租户
     */
    private String tenant;

    /**
     * 保存的操作日志的主模块，比如：订单、商品
     */
    @NotBlank(message = "type required")
    @Length(max = 200, message = "type max length is 200")
    private String type;
    /**
     * 日志的副模块，比如创建订单、修改商品
     */
    private String subType;

    /**
     * 日志绑定的业务标识
     */
    @NotBlank(message = "bizNo required")
    @Length(max = 200, message = "bizNo max length is 200")
    private String bizNo;
    /**
     * 操作人
     */
    @NotBlank(message = "operator required")
    @Length(max = 63, message = "operator max length 63")
    private String operator;

    /**
     * 日志内容（渲染后的文案）
     */
    @NotBlank(message = "opAction required")
    @Length(max = 511, message = "operator max length 511")
    private String action;
    /**
     * 是否为操作失败的日志
     */
    private boolean fail;
    /**
     * 日志的创建时间
     */
    private Date createTime;
    /**
     * 日志的额外信息
     *
     * @since 2.0.0 从detail 修改为extra
     */
    private String extra;

    /**
     * 打印日志的代码信息
     * <p>
     * CodeVariableType 日志记录的 ClassName、MethodName
     */
    private Map<CodeVariableType, Object> codeVariable;
}
