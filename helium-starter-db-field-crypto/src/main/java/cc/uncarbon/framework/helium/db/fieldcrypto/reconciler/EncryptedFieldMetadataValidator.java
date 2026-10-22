package cc.uncarbon.framework.helium.db.fieldcrypto.reconciler;

import cc.uncarbon.framework.helium.db.fieldcrypto.annotation.TableFieldCrypto;
import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.typehandler.EncryptedFieldTypeHandler;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link TableFieldCrypto} 启动期元数据校验
 *
 * <p>遍历全部 Mapper 绑定的实体，对每个标注 {@link TableFieldCrypto} 的字段：</p>
 * <ol>
 *   <li>columnPath 解析：缺省由 {@link TableName} + {@link TableField} 反射派生（表名.密文列名），
 *       显式 {@code columnPath} 仅作覆盖</li>
 *   <li>columnPath 全局去重：两个不同字段声明相同 columnPath，则拒绝启动</li>
 *   <li>TypeHandler 配对：字段的 {@code @TableField(typeHandler)} 须为 {@link EncryptedFieldTypeHandler}
 *       子类，且子类 {@code columnPath()} 返回值与解析结果一致，拒配错</li>
 * </ol>
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class EncryptedFieldMetadataValidator implements SmartInitializingSingleton {

    /**
     * columnPath -> 表.列
     */
    private static final Map<String, String> COLUMN_PATH_REGISTRY = new ConcurrentHashMap<>();

    private final SqlSessionFactory sqlSessionFactory;

    /**
     * 密文列名：优先 {@code @TableField} 显式列名，缺省退化为字段名
     */
    private static String resolveColumnName(Field field) {
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField != null && !tableField.value().isBlank()) {
            return tableField.value();
        }
        return field.getName();
    }

    private static String resolveTableName(Class<?> entityClass) {
        TableName tableName = entityClass.getAnnotation(TableName.class);
        return tableName != null ? tableName.value() : entityClass.getSimpleName();
    }

    public static Map<String, String> getColumnPathRegistrySnapshot() {
        return Map.copyOf(COLUMN_PATH_REGISTRY);
    }

    /**
     * 全部单例（含 Mapper）就绪后自动执行校验，失败即中止启动
     */
    @Override
    public void afterSingletonsInstantiated() {
        validate();
    }

    public void validate() {
        Map<String, String> seen = new HashMap<>();
        var mapperClasses = sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers();

        for (Class<?> mapperClass : mapperClasses) {
            if (!BaseMapper.class.isAssignableFrom(mapperClass)) {
                continue;
            }
            Class<?> entityClass;
            try {
                entityClass = ReflectionKit.getSuperClassGenericType(mapperClass, BaseMapper.class, 0);
            } catch (Exception e) {
                // 泛型信息不可解析的 mapper 跳过
                continue;
            }
            if (entityClass == null || entityClass == Object.class) {
                continue;
            }
            validateEntity(entityClass, seen);
        }
        log.info(FieldCryptoConstant.LOG_PREFIX + " >> 元数据校验通过，共扫描到 {} 个敏感字段", seen.size());
    }

    private void validateEntity(Class<?> entityClass, Map<String, String> seen) {
        String tableName = resolveTableName(entityClass);
        for (Class<?> clazz = entityClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                TableFieldCrypto annotation = field.getAnnotation(TableFieldCrypto.class);
                if (annotation == null) {
                    continue;
                }
                String columnName = resolveColumnName(field);
                String columnPath = annotation.columnPath().isBlank()
                        ? tableName + StrPool.DOT + columnName
                        : annotation.columnPath();
                String location = tableName + StrPool.DOT + columnName;

                String previous = seen.putIfAbsent(columnPath, location);
                if (previous != null && !previous.equals(location)) {
                    throw new IllegalStateException(String.format(
                            FieldCryptoConstant.LOG_PREFIX + " columnPath 重复出现 '%s'（%s 与 %s）",
                            columnPath, previous, location));
                }

                validateHandlerPairing(field, columnPath);
                COLUMN_PATH_REGISTRY.putIfAbsent(columnPath, location);
            }
        }
    }

    private void validateHandlerPairing(Field field, String expectedColumnPath) {
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField == null) {
            throw new IllegalStateException(String.format(
                    FieldCryptoConstant.LOG_PREFIX + " 字段 %s.%s 标注了 @TableFieldCrypto 但缺少 @TableField",
                    field.getDeclaringClass().getSimpleName(), field.getName()));
        }
        Class<?> handlerClass = tableField.typeHandler();
        if (handlerClass == void.class || !EncryptedFieldTypeHandler.class.isAssignableFrom(handlerClass)) {
            throw new IllegalStateException(String.format(
                    FieldCryptoConstant.LOG_PREFIX + " 字段 %s.%s 的 typeHandler 须为 EncryptedFieldTypeHandler 子类",
                    field.getDeclaringClass().getSimpleName(), field.getName()));
        }
        try {
            EncryptedFieldTypeHandler handler = (EncryptedFieldTypeHandler)
                    handlerClass.getDeclaredConstructor().newInstance();
            String actualColumnPath = handler.columnPath();
            if (!expectedColumnPath.equals(actualColumnPath)) {
                throw new IllegalStateException(String.format(
                        FieldCryptoConstant.LOG_PREFIX + " 字段 %s.%s 的 TypeHandler columnPath()='%s' 与解析结果 '%s' 不一致",
                        field.getDeclaringClass().getSimpleName(), field.getName(), actualColumnPath, expectedColumnPath));
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(String.format(
                    FieldCryptoConstant.LOG_PREFIX + " 字段 %s.%s 的 TypeHandler %s 实例化失败（需 public 无参构造）",
                    field.getDeclaringClass().getSimpleName(), field.getName(), handlerClass.getSimpleName()), e);
        }
    }
}
