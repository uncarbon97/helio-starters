package cc.uncarbon.framework.web.xss;

import cc.uncarbon.framework.core.exception.BusinessException;
import cc.uncarbon.framework.i18n.util.I18nUtil;
import cc.uncarbon.framework.web.enums.ErrorResponseEnum;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.util.StringUtils;

/**
 * SQL过滤
 *
 * @author Mark sunlightcs@gmail.com
 */
public class SQLFilter {

    /**
     * SQL注入过滤
     * @param str  待验证的字符串
     */
    public static String sqlInject(String str){
        if(StrUtil.isBlank(str)){
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
        ErrorResponseEnum containsIllegalCharacter = ErrorResponseEnum.CONTAINS_ILLEGAL_CHARACTER;
        for (String keyword : keywords) {
            if (str.contains(keyword)) {
                throw new BusinessException(
                        containsIllegalCharacter.getValue(),
                        ObjUtil.defaultIfEmpty(I18nUtil.messageOf(containsIllegalCharacter.name()), containsIllegalCharacter.getLabel())
                );
            }
        }

        return str;
    }
}
