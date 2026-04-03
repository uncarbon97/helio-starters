package com.ikbvip.framework.core.boot.entity;

import java.time.LocalDateTime;

/**
 * @description: 审计时间实体
 * @author: hanfeng
 **/
public interface AuditingTimeEntity extends Entity {

    /**
     * 创建时间
     */
    String CREATED_TIME = "createdTime";
    /**
     * 更新时间
     */
    String UPDATED_TIME = "updatedTime";


    /**
     * 获取创建时间
     *
     * @return
     */
    LocalDateTime getCreatedTime();


    /**
     * 获取最后修改时间
     *
     * @return
     */
    LocalDateTime getUpdatedTime();
}
