package cc.uncarbon.framework.helium.tenant.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 租户上下文持有者-嵌套安全性单测（B16）
 *
 * <p>ScopedValue 为结构化作用域语义：嵌套绑定结束自动恢复外层绑定，禁止裸 set；
 * 本用例固化该语义，防止实现回退为 set→run→clear 式污染外层现场</p>
 */
class TenantContextHolderTest {

    @Test
    void nestedContextRestoresOuter() {
        TenantContext outer = new SimpleTenantContext(1L, "outer", "outer");
        TenantContext inner = new SimpleTenantContext(2L, "inner", "inner");

        TenantContextHolder.runWithContext(outer, () -> {
            Assertions.assertEquals(1L, TenantContextHolder.getTenantId());

            TenantContextHolder.runWithContext(inner, () ->
                    Assertions.assertEquals(2L, TenantContextHolder.getTenantId()));

            // 内层作用域结束，外层上下文必须恢复，而非被清空
            Assertions.assertEquals(1L, TenantContextHolder.getTenantId());
            Assertions.assertSame(outer, TenantContextHolder.getContext());
        });

        // 最外层作用域结束，上下文不再可见
        Assertions.assertNull(TenantContextHolder.getContext());
    }

    @Test
    void nestedIgnoredRestoresOuter() {
        TenantContextHolder.runWithContext(new SimpleTenantContext(1L, null, null), () -> {
            Assertions.assertFalse(TenantContextHolder.isIgnored());

            TenantContextHolder.runIgnored(() ->
                    Assertions.assertTrue(TenantContextHolder.isIgnored()));

            Assertions.assertFalse(TenantContextHolder.isIgnored());
        });
    }

    @Test
    void callWithContextReturnsValue() throws Exception {
        Long ret = TenantContextHolder.callWithContext(
                new SimpleTenantContext(3L, null, null), () -> TenantContextHolder.getTenantId());
        Assertions.assertEquals(3L, ret);
    }

    @Test
    void contextAbsentByDefault() {
        Assertions.assertNull(TenantContextHolder.getContext());
        Assertions.assertFalse(TenantContextHolder.isIgnored());
    }
}
