package cc.uncarbon.framework.helio.mybatis.entity;

/**
 * 含有逻辑删除标识的实体
 **/
public interface LogicDelEntity {

    /**
     * @return 获取逻辑删除标识
     */
    Integer getDelFlag();
}
