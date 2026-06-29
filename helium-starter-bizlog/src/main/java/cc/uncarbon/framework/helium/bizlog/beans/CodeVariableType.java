package cc.uncarbon.framework.helium.bizlog.beans;

/**
 * 日志代码定位信息类型
 * <p>
 * 用于在 {@link LogRecord#getCodeVariable()} 中标识定位信息的种类（类名 / 方法名）。
 *
 * @author wulang@mzt-biz-log
 * @author Uncarbon
 */
public enum CodeVariableType {
    /** 打印日志的类 */
    ClassName,
    /** 打印日志的方法 */
    MethodName,
    ;
}
