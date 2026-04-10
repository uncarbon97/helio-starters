package cc.uncarbon.framework.helio.db.entity;

/**
 * 含有用户审计字段的实体
 *
 * @author Uncarbon
 */
public interface AuditingUserEntity extends Entity {

    /**
     * @return 获取创建者
     */
    String getCreatedBy();

    /**
     * @return 获取更新者
     */
    String getUpdatedBy();

}
