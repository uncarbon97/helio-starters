package cc.uncarbon.framework.helium.db.fieldcrypto.autoconfigure;

import cc.uncarbon.framework.helium.base.condition.HeliumConditions;
import cc.uncarbon.framework.helium.db.fieldcrypto.blindindex.BlindIndexCalculator;
import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.FieldCipher;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.FieldCipherContext;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.FieldKeyProvider;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.PropsFieldKeyProvider;
import cc.uncarbon.framework.helium.db.fieldcrypto.interceptor.FieldCryptoDerivedColumnInnerInterceptor;
import cc.uncarbon.framework.helium.db.fieldcrypto.props.HeliumFieldCryptoProperties;
import cc.uncarbon.framework.helium.db.fieldcrypto.reconciler.EncryptedFieldMetadataValidator;
import cc.uncarbon.framework.helium.db.fieldcrypto.reconciler.FieldCryptoCanaryChecker;
import cc.uncarbon.framework.helium.db.fieldcrypto.reconciler.FieldCryptoStartupGuard;
import cc.uncarbon.framework.helium.db.mybatisplus.autoconfigure.HeliumMPAutoConfiguration;
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
@Slf4j
@AutoConfiguration(before = HeliumMPAutoConfiguration.class)
@EnableConfigurationProperties(value = HeliumFieldCryptoProperties.class)
public class HeliumFieldCryptoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Conditional(value = OnActiveDataKeyIdPresent.class)
    public FieldKeyProvider propsFieldKeyProvider(HeliumFieldCryptoProperties props) {
        return new PropsFieldKeyProvider(props);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(value = FieldKeyProvider.class)
    public BlindIndexCalculator blindIndexCalculator(FieldKeyProvider fieldKeyProvider) {
        return new BlindIndexCalculator(fieldKeyProvider.loadIndexRootKey());
    }

    @Bean
    @ConditionalOnBean(value = BlindIndexCalculator.class)
    public FieldCryptoDerivedColumnInnerInterceptor fieldCryptoDerivedColumnInnerInterceptor(BlindIndexCalculator blindIndexCalculator) {
        return new FieldCryptoDerivedColumnInnerInterceptor(blindIndexCalculator);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(value = FieldKeyProvider.class)
    public FieldCipher fieldCipher(FieldKeyProvider fieldKeyProvider, HeliumFieldCryptoProperties props) {
        new FieldCryptoStartupGuard(props).check();

        FieldCipher cipher = new FieldCipher(
                fieldKeyProvider.loadDataRootKey(), fieldKeyProvider.loadDataKeyIds(),
                fieldKeyProvider.loadActiveDataKeyId(),
                fieldKeyProvider.loadPreviousDataRootKey(), fieldKeyProvider.loadPreviousDataKeyIds());
        // 供 MyBatis TypeHandler 静态获取
        FieldCipherContext.register(cipher);
        log.info(FieldCryptoConstant.LOG_PREFIX + " >> FieldCipher 已就绪, activeKeyId={}, previousKeyIdCount={}",
                cipher.getActiveKeyId(), fieldKeyProvider.loadPreviousDataKeyIds().size());
        return cipher;
    }

    @Bean
    @ConditionalOnBean(value = FieldCipher.class)
    public EncryptedFieldMetadataValidator encryptedFieldMetadataValidator(SqlSessionFactory sqlSessionFactory) {
        return new EncryptedFieldMetadataValidator(sqlSessionFactory);
    }

    @Bean
    @Conditional(value = OnCanaryEnabled.class)
    @ConditionalOnBean(value = FieldCipher.class)
    public FieldCryptoCanaryChecker fieldCryptoCanaryChecker(ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
                                                             FieldCipher fieldCipher,
                                                             BlindIndexCalculator blindIndexCalculator,
                                                             HeliumFieldCryptoProperties props) {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " canary 比对需要 JdbcTemplate");
        }
        return new FieldCryptoCanaryChecker(
                jdbcTemplate, fieldCipher, blindIndexCalculator,
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
