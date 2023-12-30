package cc.uncarbon.framework.crud.handler;

import cc.uncarbon.framework.core.enums.IdGeneratorStrategyEnum;
import cn.hutool.core.net.NetUtil;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义ID生成器 - Mybatis-Plus Sequence
 *
 * @author Uncarbon
 */
@Slf4j
public class HelioSequenceIdGenerateHandler extends DefaultIdentifierGenerator {

    public HelioSequenceIdGenerateHandler() {
        super(NetUtil.getLocalhost());
        log.info("[主键ID生成器] >> strategy=[{}]",
                IdGeneratorStrategyEnum.SEQUENCE
        );
    }
}
