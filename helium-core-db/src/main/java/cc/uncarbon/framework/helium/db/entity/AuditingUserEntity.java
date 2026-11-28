package cc.uncarbon.framework.helium.db.entity;

/**
 * 含有用户审计字段的实体
 *
 * @author Uncarbon
 */
public interface AuditingUserEntity extends Entity {

    /**
     * @return 获取创建者用户ID
     */
    Long getCreatedBy();

    /**
     * @return 获取更新者用户ID
     */
    Long getUpdatedBy();

}
