package cc.uncarbon.framework.helium.db.mybatisplus.handler;

import org.apache.ibatis.reflection.MetaObject;

/**
 * 字段自动填充扩展点
 *
 * @author Uncarbon
 */
public interface MetaObjectFiller {

    /**
     * 参与自动填充
     *
     * @param metaObject 实体元对象
     * @param insert     true = insert 语境；false = updateById 语境
     */
    void fill(MetaObject metaObject, boolean insert);
}
