package cc.uncarbon.framework.crud.config;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;


/**
 * 在项目启动时直接初始化Hikari连接池, 关闭按需连接
 * @author Uncarbon
 */
public class InitHikariPoolAtStartupConfiguration {

    @Bean
    public ApplicationRunner runner(DataSource dataSource) {
        return args -> dataSource.getConnection();
    }
}
