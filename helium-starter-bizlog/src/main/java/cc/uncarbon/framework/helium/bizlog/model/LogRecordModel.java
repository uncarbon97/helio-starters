package cc.uncarbon.framework.helium.bizlog.model;

import cc.uncarbon.framework.helium.bizlog.service.ILogRecordDataService;
import cc.uncarbon.framework.helium.bizlog.support.aop.LogRecordInterceptor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 业务日志实体
 * <p>
 * 一条业务操作日志的完整数据，由 {@link LogRecordInterceptor} 渲染文案后交由{@link ILogRecordDataService} 落库
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LogRecordModel {

    /**
     * 主键 id
     */
    private Serializable id;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 主模块，比如：订单、商品
     */
    private String mainModule;

    /**
     * 副模块，比如：创建订单、修改商品
     */
    private String subModule;

    /**
     * 日志绑定的业务标识
     */
    private String bizNo;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 日志内容（渲染后的文案）
     */
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
     */
    private String extra;

    /**
     * 打印日志的代码信息
     * <p>
     * CodeVariableType 日志记录的 ClassName、MethodName
     */
    private Map<CodeVariableType, Object> codeVariable;

    /**
     * 日志代码定位信息类型
     * <p>
     * 用于在 {@link LogRecordModel#getCodeVariable()} 中标识定位信息的种类（类名 / 方法名）。
     *
     * @author wulang@mzt-biz-log
     * @author Uncarbon
     */
    public enum CodeVariableType {
        /**
         * 打印日志的类
         */
        ClassName,
        /**
         * 打印日志的方法
         */
        MethodName,
        ;
    }
}
