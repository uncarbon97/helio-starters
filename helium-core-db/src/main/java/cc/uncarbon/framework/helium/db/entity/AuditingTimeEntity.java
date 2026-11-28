package cc.uncarbon.framework.helium.db.entity;

import java.time.Instant;

public interface AuditingTimeEntity extends Entity {

    /**
     * @return 获取创建时刻
     */
    Instant getCreatedAt();

    /**
     * @return 获取更新时刻
     */
    Instant getUpdatedAt();

}
