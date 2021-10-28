package cc.uncarbon.framework.crud.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import javax.sql.DataSource;


/**
 * 在项目启动时直接初始化Hikari连接池, 关闭按需连接
 * @author Uncarbon
 */
public class InitHikariPoolAtStartupConfiguration {

    @Resource
    private DataSource dataSource;


    @Bean
    public ApplicationRunner runner() {
        return args -> dataSource.getConnection();
    }
}
