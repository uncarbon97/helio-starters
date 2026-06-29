package cc.uncarbon.framework.helium.bizlog.diff;

import de.danielbechler.diff.node.DiffNode;

/**
 * 差异节点转文案服务
 * <p>
 * 将 {@link DiffNode} 差异结果转换为可读的日志文案，使用方可自定义实现以定制输出格式。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
public interface IDiffItemsToLogContentService {

    /**
     * 将差异节点转为日志文案。
     *
     * @param diffNode 差异节点
     * @param o1       旧对象
     * @param o2       新对象
     * @return 差异文案
     */
    String toLogContent(DiffNode diffNode, final Object o1, final Object o2);
}
