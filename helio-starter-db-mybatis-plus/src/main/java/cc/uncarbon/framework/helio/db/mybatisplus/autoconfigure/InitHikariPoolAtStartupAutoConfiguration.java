package cc.uncarbon.framework.helio.db.mybatisplus.autoconfigure;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;


/**
 * 在项目启动时直接初始化 Hikari 连接池（否则默认为懒加载）
 *
 * @author Uncarbon
 */
public class InitHikariPoolAtStartupAutoConfiguration {

    @Bean
    public ApplicationRunner warmUpHikariDataSource(DataSource dataSource) {
        return args -> {
            if (dataSource instanceof HikariDataSource hikariDataSource) {
                hikariDataSource.getConnection().close();
            }
        };
    }
}
