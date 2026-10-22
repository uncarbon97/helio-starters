package cc.uncarbon.framework.helium.db.fieldcrypto.autoconfigure;

import cc.uncarbon.framework.helium.base.condition.HeliumConditions;
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
import org.apache.ibatis.session.SqlSessionFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    @Conditional(value = OnActiveDataKeyIdPresent.class)
    @ConditionalOnMissingBean
    @Bean
    public FieldKeyProvider propsFieldKeyProvider(HeliumFieldCryptoProperties props) {
        return new PropsFieldKeyProvider(props);
    }

    @ConditionalOnBean(value = FieldKeyProvider.class)
    @ConditionalOnMissingBean
    @Bean
    public BlindIndex blindIndex(FieldKeyProvider fieldKeyProvider) {
        return new BlindIndex(fieldKeyProvider.loadIndexRootKey());
    }

    @ConditionalOnBean(value = BlindIndex.class)
    @Bean
    public MetaObjectFiller fieldCryptoDerivedColumnFiller(BlindIndex blindIndex) {
        return new FieldCryptoDerivedColumnFiller(blindIndex);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean
    public FieldCipher fieldCipher(FieldKeyProvider fieldKeyProvider, HeliumFieldCryptoProperties props) {
        new FieldCryptoStartupGuard(props).check();

        FieldCipher cipher = new FieldCipher(
                fieldKeyProvider.loadDataRootKey(), fieldKeyProvider.loadDataKeyIds(),
                fieldKeyProvider.loadActiveDataKeyId());
        // 供 MyBatis TypeHandler 静态获取
        FieldCipherContext.register(cipher);
        log.info(FieldCryptoConstant.LOG_PREFIX + " >> FieldCipher 已就绪, activeKeyId={}", cipher.getActiveKeyId());
        return cipher;
    }

    @ConditionalOnBean(value = FieldCipher.class)
    @Bean
    public EncryptedFieldMetadataValidator encryptedFieldMetadataValidator(SqlSessionFactory sqlSessionFactory) {
        return new EncryptedFieldMetadataValidator(sqlSessionFactory);
    }

    @Conditional(value = OnCanaryEnabled.class)
    @ConditionalOnBean(value = FieldCipher.class)
    @Bean
    public FieldCryptoCanaryChecker fieldCryptoCanaryChecker(ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
                                                             FieldCipher fieldCipher,
                                                             BlindIndex blindIndex,
                                                             HeliumFieldCryptoProperties props) {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " canary 比对需要 JdbcTemplate");
        }
        return new FieldCryptoCanaryChecker(
                jdbcTemplate, fieldCipher, blindIndex,
                props.getCanary().isEnabled(), props.getCanary().isAllowOverwrite());
    }

    private static class OnActiveDataKeyIdPresent implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = HeliumConditions.bind(context.getEnvironment(), HeliumFieldCryptoProperties.class);
            var sub = props.getActiveDataKeyId();
            return sub != null && !sub.isBlank();
        }
    }

    private static class OnCanaryEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = HeliumConditions.bind(context.getEnvironment(), HeliumFieldCryptoProperties.class);
            return props.getCanary().isEnabled();
        }
    }

}
