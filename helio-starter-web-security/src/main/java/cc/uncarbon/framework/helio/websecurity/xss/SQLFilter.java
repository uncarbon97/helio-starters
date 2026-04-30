package cc.uncarbon.framework.helio.websecurity.xss;

import cc.uncarbon.framework.helio.base.exception.BusinessException;
import cn.hutool.core.text.CharSequenceUtil;
import org.springframework.util.StringUtils;

/**
 * SQL过滤
 *
 * @author Mark@renren-framework
 */
public final class SQLFilter {

    private SQLFilter() {
    }

    /**
     * SQL注入过滤
     *
     * @param str 待验证的字符串
     */
    public static String sqlInject(String str) {
        if (CharSequenceUtil.isBlank(str)) {
            return null;
        }
        // 去掉'|"|;|\字符
        str = StringUtils.replace(str, "'", "");
        str = StringUtils.replace(str, "\"", "");
        str = StringUtils.replace(str, ";", "");
        str = StringUtils.replace(str, "\\", "");

        // 转换成小写
        str = str.toLowerCase();

        // 非法字符
        String[] keywords = {"master", "truncate", "insert", "select", "delete", "update", "declare", "alter", "drop"};

        // 判断是否包含非法字符
        for (String keyword : keywords) {
            if (str.contains(keyword)) {
                throw new BusinessException("Contains illegal character");
            }
        }

        return str;
    }
}
