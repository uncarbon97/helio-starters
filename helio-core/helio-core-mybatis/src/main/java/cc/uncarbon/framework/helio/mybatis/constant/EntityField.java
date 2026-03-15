package cc.uncarbon.framework.helio.mybatis.constant;

/**
 * 实体类字段常量
 *
 * @author Uncarbon
 */
public final class EntityField {
    private EntityField() {
    }

    /**
     * 租户ID
     */
    public static final String COLUMN_TENANT_ID = "tenant_id";
    public static final String FIELD_TENANT_ID = "tenantId";

    /**
     * 创建时刻
     */
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String FIELD_CREATED_AT = "createdAt";

    /**
     * 创建者
     */
    public static final String COLUMN_CREATED_BY = "created_by";
    public static final String FIELD_CREATED_BY = "createdBy";

    /**
     * 更新时刻
     */
    public static final String COLUMN_UPDATED_AT = "updated_at";
    public static final String FIELD_UPDATED_AT = "updatedAt";

    /**
     * 更新者
     */
    public static final String COLUMN_UPDATED_BY = "updated_by";
    public static final String FIELD_UPDATED_BY = "updatedBy";

}
