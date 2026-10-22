package cc.uncarbon.framework.helium.db.fieldcrypto.autoconfigure;

import cc.uncarbon.framework.helium.base.condition.HeliumConditions;
import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.db.fieldcrypto.blindindex.BlindIndex;
import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.*;
import cc.uncarbon.framework.helium.db.fieldcrypto.props.HeliumFieldCryptoProperties;
import cc.uncarbon.framework.helium.db.fieldcrypto.reconciler.EncryptedFieldMetadataValidator;
import cc.uncarbon.framework.helium.db.fieldcrypto.reconciler.FieldCryptoCanaryChecker;
import cc.uncarbon.framework.helium.db.fieldcrypto.reconciler.FieldCryptoStartupGuard;
import cc.uncarbon.framework.helium.db.mybatisplus.autoconfigure.HeliumMPAutoConfiguration;
import cc.uncarbon.framework.helium.db.mybatisplus.handler.MetaObjectFiller;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Helium 字段加密自动装配类
 *
 * @author Uncarbon
 */
@AutoConfiguration(before = HeliumMPAutoConfiguration.class)
@EnableConfigurationProperties(value = HeliumFieldCryptoProperties.class)
@Slf4j
public class HeliumFieldCryptoAutoConfiguration {

    /**
     * 字段加密密钥提供方
     */
    @Conditional(value = OnActiveDataKeyIdPresent.class)
    @ConditionalOnMissingBean
    @Bean
    public FieldKeyProvider propsFieldKeyProvider(HeliumFieldCryptoProperties props) {
        return new PropsFieldKeyProvider(props);
    }

    /**
     * 字段加密器：fail-closed 启动闸（任务 1.3）+ 根密钥/keyId 校验在此 fail-fast
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean
    public FieldCipher fieldCipher(FieldKeyProvider fieldKeyProvider,
                                   HeliumFieldCryptoProperties props) {
        // fail-closed：密钥缺失/Base64 非法/长度不符/keyId 字符集不合法 → 拒启
        new FieldCryptoStartupGuard(props).check();

        FieldCipher cipher = new FieldCipher(
                fieldKeyProvider.loadDataRootKey(), fieldKeyProvider.loadDataKeyIds(),
                fieldKeyProvider.loadActiveDataKeyId());
        // 供 MyBatis TypeHandler 静态获取
        FieldCipherContext.register(cipher);
        log.info(FieldCryptoConstant.LOG_PREFIX + " >> FieldCipher 已就绪, activeKeyId={}", cipher.getActiveKeyId());
        return cipher;
    }

    /**
     * 盲索引计算器（索引根密钥按列 HKDF 派生 IK，截断 128bit 冻结）
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean
    public BlindIndex blindIndex(FieldKeyProvider fieldKeyProvider) {
        return new BlindIndex(fieldKeyProvider.loadIndexRootKey());
    }

    /**
     * 根密钥双指纹 canary 启动比对（任务 1.7）：任一指纹不匹配 = 根密钥代际漂移 → 拒启；
     * 轮换覆写走 canary.allow-overwrite 维护开关（单次生效 + 审计）
     */
    @Bean(FieldCryptoCanaryChecker.class.getSimpleName())
    @ConditionalOnBean(FieldCipher.class)
    @ConditionalOnProperty(prefix = ConfigurationPropertiesPrefix.DB_FIELD_CRYPTO, name = "canary.enabled",
            havingValue = "true", matchIfMissing = true)
    public FieldCryptoCanaryChecker fieldCryptoCanaryChecker(ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
                                                             FieldCipher fieldCipher,
                                                             BlindIndex blindIndex,
                                                             HeliumFieldCryptoProperties props) {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException(
                    FieldCryptoConstant.LOG_PREFIX + " fail-closed: canary 比对需要 JdbcTemplate（请确认 spring-boot-starter-jdbc / 数据源配置）");
        }
        return new FieldCryptoCanaryChecker(
                jdbcTemplate, fieldCipher, blindIndex,
                props.getCanary().isEnabled(), props.getCanary().isAllowOverwrite());
    }

    /**
     * 派生列填充贡献者：@BlindIndexOf / @MaskedOf 列派生填充 + 一致性双向校验
     */
    @Bean
    @ConditionalOnBean(BlindIndex.class)
    public MetaObjectFiller fieldCryptoDerivedColumnFiller(BlindIndex blindIndex) {
        return new FieldCryptoDerivedColumnFiller(blindIndex);
    }

    /**
     * @TableFieldCrypto 元数据启动校验：columnPath 反射派生/全局去重 + TypeHandler 配对
     */
    @Bean(EncryptedFieldMetadataValidator.class.getSimpleName())
    @ConditionalOnBean(value = FieldCipher.class)
    public EncryptedFieldMetadataValidator encryptedFieldMetadataValidator(
            org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory) {
        return new EncryptedFieldMetadataValidator(sqlSessionFactory);
    }

    private static class OnActiveDataKeyIdPresent implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = HeliumConditions.bind(context.getEnvironment(), HeliumFieldCryptoProperties.class);
            var sub = props.getActiveDataKeyId();
            return sub != null && !sub.isBlank();
        }
    }

}
