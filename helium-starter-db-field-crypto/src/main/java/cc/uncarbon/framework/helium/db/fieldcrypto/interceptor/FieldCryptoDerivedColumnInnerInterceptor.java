package cc.uncarbon.framework.helium.db.fieldcrypto.interceptor;

import cc.uncarbon.framework.helium.db.fieldcrypto.annotation.BlindIndexOf;
import cc.uncarbon.framework.helium.db.fieldcrypto.annotation.MaskedOf;
import cc.uncarbon.framework.helium.db.fieldcrypto.blindindex.BlindIndexCalculator;
import cc.uncarbon.framework.helium.db.fieldcrypto.blindindex.BlindIndexValueExtractor;
import cc.uncarbon.framework.helium.db.fieldcrypto.exception.FieldCryptoException;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.FieldCryptoMetrics;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.FieldValueMasker;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段加密派生字段回填拦截器：盲索引字段、脱敏冗余字段自动填充
 * <p>填充规则</p>
 * <ul>
 *   <li>insert：来源字段为 null/空白时，派生字段一并置 null，保持同生同灭</li>
 *   <li>update：来源字段未携带（null）时跳过，避免局部更新误清既有索引</li>
 *   <li>update 显式清空走业务侧专用清空方法（全部派生字段一并置 NULL），本拦截器双向校验兜底</li>
 * </ul>
 *
 * <p>派生字段一致性双向校验，回填完成后——
 * <p>1. 密文来源非空而任一派生字段为空，或来源为空而任一派生字段非空（清空残留），拒写
 * <p>2. {@code update(null, wrapper)} 纯 wrapper 更新、原生 SQL 直改派生字段不经过本拦截器，禁用</p>
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class FieldCryptoDerivedColumnInnerInterceptor implements InnerInterceptor {

    /**
     * 实体类 -> 派生字段回填计划（反射元数据缓存）
     */
    private static final Map<Class<?>, EntityFillPlan> FILL_PLAN_CACHE = new ConcurrentHashMap<>();

    /**
     * ParamMap 中实体参数名，见 {@link Constants#ENTITY}
     */
    private static final String ENTITY_PARAM = Constants.ENTITY;

    /**
     * ParamMap 中批量集合参数名（MyBatis ParamNameResolver 约定）
     */
    private static final List<String> BATCH_PARAMS = List.of("list", "collection", "array");

    /**
     * 盲索引计算器；为 null（未启用字段加密）时视为配置错误
     */
    private final BlindIndexCalculator blindIndexCalculator;


    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        SqlCommandType commandType = ms.getSqlCommandType();
        if (commandType != SqlCommandType.INSERT && commandType != SqlCommandType.UPDATE) {
            return;
        }
        processParameter(parameter, commandType == SqlCommandType.INSERT);
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    private static <T> T newInstance(Class<? extends T> type, String role) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    role + "实例化失败: " + type.getSimpleName() + "，需提供 public 无参构造", e);
        }
    }

    /**
     * 从 Mapper 入参中剥离实体（或批量实体集），再按计划回填
     */
    private void processParameter(Object parameter, boolean insert) {
        switch (parameter) {
            case null -> {
                return;
            }
            case Map<?, ?> paramMap -> {
                // MapperMethod.ParamMap#get 对缺失键直接抛 BindingException，必须先 containsKey
                Object entity = paramMap.getOrDefault(ENTITY_PARAM, null);
                if (entity != null) {
                    fillIfPlanned(entity, insert);
                    return;
                }
                for (String param : BATCH_PARAMS) {
                    if (!paramMap.containsKey(param)) {
                        continue;
                    }
                    if (paramMap.get(param) instanceof Collection<?> entities) {
                        entities.forEach(e -> fillIfPlanned(e, insert));
                        return;
                    }
                }
                return;
            }
            case Collection<?> entities -> {
                // 未标注 @Param 的集合参数（如 insertBatchSomeColumn 直收集合）
                entities.forEach(e -> fillIfPlanned(e, insert));
                return;
            }
            default -> {
            }
        }
        fillIfPlanned(parameter, insert);
    }

    private void fillIfPlanned(Object entity, boolean insert) {
        EntityFillPlan plan = FILL_PLAN_CACHE.computeIfAbsent(entity.getClass(), this::resolveFillPlan);
        if (plan.groups().isEmpty()) {
            return;
        }
        if (blindIndexCalculator == null) {
            // 实体声明了派生字段，但字段加密未启用 = 配置错误
            FieldCryptoMetrics.incrementDerivedColumnRejects();
            throw new FieldCryptoException(FieldCryptoException.Reason.NOT_CONFIGURED,
                    "实体声明了 @BlindIndexOf/@MaskedOf 派生字段，但字段加密未启用");
        }
        fill(SystemMetaObject.forObject(entity), plan, insert);
    }

    private void fill(MetaObject metaObject, EntityFillPlan plan, boolean insert) {
        for (SourceDeriveGroup group : plan.groups()) {
            if (!metaObject.hasGetter(group.sourceField())) {
                continue;
            }
            Object sourceValue = metaObject.getValue(group.sourceField());
            String source = sourceValue == null ? null : String.valueOf(sourceValue);
            boolean sourcePresent = source != null && !source.isBlank();

            if (sourceValue == null && !insert) {
                // update：来源未携带，跳过回填（防局部更新误清既有索引）；校验派生字段无手工残留即可
                validateNoResidue(metaObject, group);
                continue;
            }

            for (BlindIndexPlan p : group.blindIndexPlans()) {
                if (!metaObject.hasSetter(p.targetField())) {
                    continue;
                }
                String normalized = source == null ? null : p.extractor().extract(source);
                String indexValue = normalized == null ? null : blindIndexCalculator.compute(p.purpose(), normalized);
                metaObject.setValue(p.targetField(), indexValue);
            }
            for (MaskedPlan p : group.maskedPlans()) {
                if (!metaObject.hasSetter(p.targetField())) {
                    continue;
                }
                String masked = source == null ? null : p.masker().mask(source);
                metaObject.setValue(p.targetField(), masked);
            }

            validateConsistency(metaObject, group, sourcePresent);
        }
    }

    /**
     * 双向一致性校验
     * <P>只输出属性名与方向，不输出任何值
     */
    private void validateConsistency(MetaObject metaObject, SourceDeriveGroup group, boolean sourcePresent) {
        List<String> nullDerived = new ArrayList<>();
        List<String> nonNullDerived = new ArrayList<>();
        collectDerivedState(metaObject, group, nullDerived, nonNullDerived);

        if (sourcePresent && !nullDerived.isEmpty()) {
            FieldCryptoMetrics.incrementDerivedColumnRejects();
            throw new FieldCryptoException(FieldCryptoException.Reason.DERIVED_COLUMN_MISMATCH,
                    "派生字段不一致：字段 [" + group.sourceField() + "] 非空，但派生字段为空 " + nullDerived
                            + "（检查 extractor/masker 对该值是否返回 null）");
        }
        if (!sourcePresent && !nonNullDerived.isEmpty()) {
            FieldCryptoMetrics.incrementDerivedColumnRejects();
            throw new FieldCryptoException(FieldCryptoException.Reason.DERIVED_COLUMN_MISMATCH,
                    "派生字段残留：字段 [" + group.sourceField() + "] 为空，但派生字段非空 " + nonNullDerived
                            + "（显式清空必须走专用清空方法，全部派生字段一并置 NULL）");
        }
    }

    /**
     * update 分支跳过回填时的残留校验（清空残留，方案 §3.3）
     */
    private void validateNoResidue(MetaObject metaObject, SourceDeriveGroup group) {
        List<String> nullDerived = new ArrayList<>();
        List<String> nonNullDerived = new ArrayList<>();
        collectDerivedState(metaObject, group, nullDerived, nonNullDerived);
        if (!nonNullDerived.isEmpty()) {
            FieldCryptoMetrics.incrementDerivedColumnRejects();
            throw new FieldCryptoException(FieldCryptoException.Reason.DERIVED_COLUMN_MISMATCH,
                    "派生字段残留：字段 [" + group.sourceField() + "] 未携带，但派生字段非空 " + nonNullDerived
                            + "（禁止手工赋值派生字段；显式清空走专用清空方法）");
        }
    }

    private void collectDerivedState(MetaObject metaObject, SourceDeriveGroup group,
                                     List<String> nullDerived, List<String> nonNullDerived) {
        for (BlindIndexPlan p : group.blindIndexPlans()) {
            if (!metaObject.hasGetter(p.targetField())) {
                continue;
            }
            Object v = metaObject.getValue(p.targetField());
            (v == null ? nullDerived : nonNullDerived).add(p.targetField());
        }
        for (MaskedPlan p : group.maskedPlans()) {
            if (!metaObject.hasGetter(p.targetField())) {
                continue;
            }
            Object v = metaObject.getValue(p.targetField());
            (v == null ? nullDerived : nonNullDerived).add(p.targetField());
        }
    }

    /**
     * 扫描实体类（含父类）中标注 {@link BlindIndexOf} / {@link MaskedOf} 的字段，按来源字段聚合生成回填计划
     */
    private EntityFillPlan resolveFillPlan(Class<?> entityClass) {
        Map<String, List<BlindIndexPlan>> blindBySource = new HashMap<>();
        Map<String, List<MaskedPlan>> maskedBySource = new HashMap<>();

        for (Class<?> clazz = entityClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                BlindIndexOf blind = field.getAnnotation(BlindIndexOf.class);
                if (blind != null) {
                    blindBySource.computeIfAbsent(blind.value(), k -> new ArrayList<>())
                            .add(new BlindIndexPlan(
                                    field.getName(), blind.purpose(), newInstance(blind.extractor(), "盲索引提取器")));
                }
                MaskedOf masked = field.getAnnotation(MaskedOf.class);
                if (masked != null) {
                    maskedBySource.computeIfAbsent(masked.value(), k -> new ArrayList<>())
                            .add(new MaskedPlan(
                                    field.getName(), newInstance(masked.masker(), "脱敏器")));
                }
            }
        }
        if (blindBySource.isEmpty() && maskedBySource.isEmpty()) {
            return EntityFillPlan.EMPTY;
        }

        List<SourceDeriveGroup> groups = new ArrayList<>();
        for (String source : blindBySource.keySet()) {
            groups.add(new SourceDeriveGroup(
                    source,
                    blindBySource.getOrDefault(source, List.of()),
                    maskedBySource.getOrDefault(source, List.of())));
        }
        for (String source : maskedBySource.keySet()) {
            if (!blindBySource.containsKey(source)) {
                groups.add(new SourceDeriveGroup(source, List.of(), maskedBySource.get(source)));
            }
        }
        return new EntityFillPlan(List.copyOf(groups));
    }

    /**
     * 单个实体类的派生字段回填计划
     */
    private record EntityFillPlan(List<SourceDeriveGroup> groups) {

        static final EntityFillPlan EMPTY = new EntityFillPlan(List.of());
    }

    /**
     * 单个来源字段的完整派生组
     */
    private record SourceDeriveGroup(
            String sourceField,
            List<BlindIndexPlan> blindIndexPlans,
            List<MaskedPlan> maskedPlans
    ) {
    }

    private record BlindIndexPlan(
            String targetField,
            String purpose,
            BlindIndexValueExtractor extractor
    ) {
    }

    private record MaskedPlan(
            String targetField,
            FieldValueMasker masker
    ) {
    }
}
