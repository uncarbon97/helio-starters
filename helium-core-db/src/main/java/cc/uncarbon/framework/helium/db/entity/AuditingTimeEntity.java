package cc.uncarbon.framework.helium.db.entity;

import java.time.LocalDateTime;

public interface AuditingTimeEntity extends Entity {

    /**
     * @return 获取创建时刻
     */
    LocalDateTime getCreatedAt();

    /**
     * @return 获取更新时刻
     */
    LocalDateTime getUpdatedAt();

}
