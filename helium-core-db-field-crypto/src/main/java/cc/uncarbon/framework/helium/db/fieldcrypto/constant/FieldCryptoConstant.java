package cc.uncarbon.framework.helium.db.fieldcrypto.constant;

import java.util.regex.Pattern;

/**
 * 字段加密模块冻结常量
 *
 * <p>以下取值均为密码学协议参数（HKDF salt/info、算法名、长度、分隔符、字符集约束等），
 * 一经正式使用不得变更，否则存量密文将无法解密、存量盲索引将检索失配</p>
 *
 * @author Uncarbon
 */
public class FieldCryptoConstant {
    /**
     * 数据根密钥英文表达，连字符形式
     */
    public static final String DATA_ROOT_KEY_KEBAB = "data-root-key";

    /*
    ----------------------------------------------------------------
                        通用
    ----------------------------------------------------------------
     */

    public static final String LOG_PREFIX = "[Framework][字段加密]";

    /**
     * 根密钥长度：32 字节 = 256bit（数据加密根密钥、索引根密钥共用）
     */
    public static final int ROOT_KEY_BYTES = 32;

    /**
     * MAC 算法名：HMAC-SHA256（盲索引计算、canary 指纹派生共用）
     */
    public static final String MAC_ALGORITHM = "HmacSHA256";

    /**
     * canary 指纹专用 keyId（非真实密钥代，仅用于根密钥代际指纹派生）
     */
    public static final String CANARY_KEY_ID = "helium.canary";

    /*
    ----------------------------------------------------------------
                        字段加密（AES-256-GCM）
    ----------------------------------------------------------------
     */
    /**
     * 盲索引根密钥英文表达，连字符形式
     */
    public static final String INDEX_ROOT_KEY_KEBAB = "index-root-key";

    /**
     * 加密变换：AES-GCM（认证加密，无填充）
     */
    public static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * 对称密钥算法名
     */
    public static final String AES_ALGORITHM = "AES";

    /**
     * GCM nonce 长度：12 字节（NIST SP 800-38D 推荐值）
     */
    public static final int GCM_NONCE_BYTES = 12;

    /**
     * GCM 认证标签长度：128bit
     */
    public static final int GCM_TAG_BITS = 128;

    /**
     * 密文 keyId 前缀分隔符；keyId 字符集已排除该字符
     */
    public static final char CIPHERTEXT_SEPARATOR = '$';

    /**
     * keyId 字符集：限 [A-Za-z0-9_-]{1,16}；keyId 含 '$' 会破坏密文前缀路由
     */
    public static final Pattern KEY_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,16}$");

    /**
     * data-key HKDF 派生 info 前缀，最终格式 {@code field-data-key/{keyId}}
     */
    public static final String DATA_KEY_INFO_PREFIX = "field-data-key/";

    /*
    ----------------------------------------------------------------
                        盲索引（HMAC-SHA256）
    ----------------------------------------------------------------
     */

    /**
     * 用途标识字符集：小写字母/数字/下划线/点/连字符
     */
    public static final Pattern PURPOSE_PATTERN = Pattern.compile("^[a-z0-9_.-]{3,128}$");
    /**
     * index-key HKDF 派生 info 前缀
     */
    public static final String INDEX_KEY_INFO_PREFIX = INDEX_KEY_SALT + "/";

    /**
     * index-key HKDF 派生 salt
     */
    public static final String INDEX_KEY_SALT = "field-index-key";

    private FieldCryptoConstant() {
    }

    /**
     * 盲索引截断字节数：16 字节 = 128bit
     */
    public static final int BLIND_INDEX_TRUNCATED_BYTES = 16;

    /**
     * 盲索引值长度：16 字节 Base64（含 padding）刚好长度 = 24
     */
    public static final int BLIND_INDEX_VALUE_LENGTH = 24;
}
