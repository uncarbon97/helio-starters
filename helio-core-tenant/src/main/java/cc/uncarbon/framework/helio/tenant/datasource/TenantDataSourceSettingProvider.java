package cc.uncarbon.framework.helio.tenant.datasource;

import cc.uncarbon.framework.helio.db.model.DataSourceSetting;

import java.util.Optional;

/**
 * 租户数据源属性配置提供者
 *
 * @author Uncarbon
 */
public interface TenantDataSourceSettingProvider {

    /**
     * 获取指定租户的数据源配置。
     *
     * @param tenantId 租户 ID
     * @return 数据源配置；若该租户未配置数据源则返回 {@link Optional#empty()}
     */
    Optional<DataSourceSetting> getByTenantId(Long tenantId);

}
