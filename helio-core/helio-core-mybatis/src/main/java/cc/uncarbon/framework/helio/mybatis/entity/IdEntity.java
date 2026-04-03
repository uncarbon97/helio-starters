package cc.uncarbon.framework.helio.mybatis.entity;

/**
 * 含有主键ID的实体
 *
 * @author Uncarbon
 */
public interface IdEntity<PK> extends Entity {

    /**
     * @return 获取主键ID
     */
    PK getId();

    /**
     * 设置主键ID
     */
    void setId(PK id);

}