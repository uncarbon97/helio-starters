package cc.uncarbon.framework.helium.db.fieldcrypto.field;

import cc.uncarbon.framework.helium.db.fieldcrypto.annotation.BlindIndexOf;
import cc.uncarbon.framework.helium.db.fieldcrypto.annotation.MaskedOf;
import cc.uncarbon.framework.helium.db.fieldcrypto.blindindex.BlindIndex;
import cc.uncarbon.framework.helium.db.fieldcrypto.blindindex.BlindIndexValueExtractor;
import cc.uncarbon.framework.helium.db.fieldcrypto.exception.FieldCryptoException;
import cc.uncarbon.framework.helium.db.mybatisplus.handler.MetaObjectFiller;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段加密派生字段填充器：盲索引字段、脱敏冗余字段自动填充
 *
 * <p>填充规则</p>
 * <ul>
 *   <li>insert：来源字段为 null/空白时，派生字段一并置 null，保持同生同灭</li>
 *   <li>update：来源字段未携带（null）时跳过，避免部分更新误清既有索引</li>
 *   <li>update 显式清空走业务侧专用清空方法（全部派生字段一并置 NULL），本填充器双向校验兜底</li>
 * </ul>
 *
 * <p>派生字段一致性双向校验，填充完成后——
 * <p>1. 密文来源非空而任一派生字段为空，或来源为空而任一派生字段非空（清空残留），拒写
 * <p>2. {@link UpdateWrapper} 局部更新、原生 SQL 直改派生字段不经过本填充器，禁用
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class FieldCryptoDerivedColumnFiller implements MetaObjectFiller {

    /**
     * 实体类 -> 派生字段填充计划（反射元数据缓存）
     */
    private static final Map<Class<?>, EntityFillPlan> FILL_PLAN_CACHE = new ConcurrentHashMap<>();
    /**
     * 盲索引计算器；为 null（未启用字段加密）时视为配置错误，fail-closed
     */
    private final BlindIndex blindIndex;

    private static <T> T newInstance(Class<? extends T> type, String role) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    role + "实例化失败: " + type.getSimpleName() + "，需提供 public 无参构造", e);
        }
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    @Override
    public void fill(MetaObject metaObject, boolean insert) {
        EntityFillPlan plan = FILL_PLAN_CACHE.computeIfAbsent(
                metaObject.getOriginalObject().getClass(), this::resolveFillPlan);
        if (plan.groups().isEmpty()) {
            return;
        }
        if (blindIndex == null) {
            // 实体声明了派生字段，但字段加密未启用 = 配置错误，fail-closed
            FieldCryptoMetrics.incrementDerivedColumnRejects();
            throw new FieldCryptoException(FieldCryptoException.Reason.NOT_CONFIGURED,
                    "实体声明了 @BlindIndexOf/@MaskedOf 派生字段，但字段加密未启用");
        }

        for (SourceDeriveGroup group : plan.groups()) {
            if (!metaObject.hasGetter(group.sourceField())) {
                continue;
            }
            Object sourceValue = metaObject.getValue(group.sourceField());
            String source = sourceValue == null ? null : String.valueOf(sourceValue);
            boolean sourcePresent = source != null && !source.isBlank();

            if (sourceValue == null && !insert) {
                // update：来源未携带，跳过填充（防部分更新误清既有索引）；校验派生字段无手工残留即可
                validateNoResidue(metaObject, group);
                continue;
            }

            for (BlindIndexPlan p : group.blindIndexPlans()) {
                if (!metaObject.hasSetter(p.targetField())) {
                    continue;
                }
                String normalized = source == null ? null : p.extractor().extract(source);
                String indexValue = normalized == null ? null : blindIndex.compute(p.purpose(), normalized);
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
     * update 分支跳过填充时的残留校验（清空残留，方案 §3.3）
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
     * 扫描实体类（含父类）中标注 @BlindIndexOf / @MaskedOf 的字段，按来源字段聚合生成填充计划
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
                            .add(new MaskedPlan(field.getName(), newInstance(masked.masker(), "脱敏器")));
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
     * 单个实体类的派生字段填充计划
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
