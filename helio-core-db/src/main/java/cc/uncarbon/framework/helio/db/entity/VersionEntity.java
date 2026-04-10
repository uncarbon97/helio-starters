package cc.uncarbon.framework.helio.db.entity;

/**
 * 含有乐观锁版本号的实体
 *
 * @author Uncarbon
 */
public interface VersionEntity {

    /**
     * @return 乐观锁版本号
     */
    Long getRevision();

}
