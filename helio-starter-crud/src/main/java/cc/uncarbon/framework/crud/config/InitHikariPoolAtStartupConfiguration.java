package cc.uncarbon.framework.crud.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;


/**
 * 在项目启动时直接初始化Hikari连接池, 关闭按需连接
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class InitHikariPoolAtStartupConfiguration {

    private final DataSource dataSource;


    @Bean
    public ApplicationRunner runner() {
        return args -> dataSource.getConnection();
    }
}
