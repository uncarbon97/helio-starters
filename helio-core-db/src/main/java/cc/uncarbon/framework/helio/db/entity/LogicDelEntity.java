package cc.uncarbon.framework.helio.db.entity;

/**
 * 含有逻辑删除标识的实体
 *
 * @author Uncarbon
 */
public interface LogicDelEntity {

    /**
     * @return 获取逻辑删除标识
     */
    Integer getDelFlag();

}
