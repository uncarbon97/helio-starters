package cc.uncarbon.framework.helio.db.entity;

/**
 * 含有主键 ID 的实体
 *
 * @author Uncarbon
 */
public interface IdEntity<PK> extends Entity {

    /**
     * @return 获取主键 ID
     */
    PK getId();

    /**
     * 设置主键 ID
     */
    void setId(PK id);

}