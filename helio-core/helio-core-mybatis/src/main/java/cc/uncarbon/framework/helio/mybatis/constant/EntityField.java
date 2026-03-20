package cc.uncarbon.framework.helio.mybatis.constant;

/**
 * 实体类字段常量
 * 包含字段名，与对应的数据表列名
 *
 * @author Uncarbon
 */
public final class EntityField {
    private EntityField() {
    }

    /**
     * 租户ID
     */
    public static final String TENANT_ID_COLUMN = "tenant_id";
    public static final String TENANT_ID_FIELD = "tenantId";

    /**
     * 创建时刻
     */
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String CREATED_AT_FIELD = "createdAt";

    /**
     * 创建者
     */
    public static final String CREATED_BY_COLUMN = "created_by";
    public static final String CREATED_BY_FIELD = "createdBy";

    /**
     * 更新时刻
     */
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String UPDATED_AT_FIELD = "updatedAt";

    /**
     * 更新者
     */
    public static final String UPDATED_BY_COLUMN = "updated_by";
    public static final String UPDATED_BY_FIELD = "updatedBy";

}
