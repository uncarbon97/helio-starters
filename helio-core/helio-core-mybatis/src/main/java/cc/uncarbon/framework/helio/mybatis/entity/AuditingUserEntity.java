package com.ikbvip.framework.core.boot.entity;

/**
 * @description: 后台用户审计
 * @author: hanfeng
 * @date: 2020-5-10
 **/
public interface AuditingUserEntity extends Entity {

    /**
     * 创建人
     */
    String CREATED_BY = "createdBy";
    /**
     * 更新人
     */
    String UPDATED_BY = "updatedBy";


    /**
     * 获取创建人
     *
     * @return
     */
    String getCreatedBy();


    /**
     * 获取最后修改人
     *
     * @return
     */
    String getUpdatedBy();
}
