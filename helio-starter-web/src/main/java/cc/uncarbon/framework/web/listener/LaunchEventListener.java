package cc.uncarbon.framework.web.listener;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 项目启动事件通知
 *
 * @author Zhu JW
 * @author Uncarbon
 **/
@Slf4j
public class LaunchEventListener {
    @Async
    @Order
    @EventListener(WebServerInitializedEvent.class)
    public void afterStart(WebServerInitializedEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        int port = event.getWebServer().getPort();

        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }

        String contextPath = env.getProperty("server.servlet.context-path");
        if (StrUtil.isBlank(contextPath)) {
            contextPath = "/";
        }

        String externalHost = "localhost";
        try {
            externalHost = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("获取外部(局域网/公网)IP失败");
        }

        System.out.println(
                StrUtil.format(
                        "\n"+
                                "----------------------------------------------------------" +
                                "\n\tApplication '{}' is running! Access URLs:" +
                                "\n\tLocal: \t\t{}://127.0.0.1:{}{}" +
                                "\n\tExternal: \t{}://{}:{}{}" +
                                "\n----------------------------------------------------------" +
                                "\n",
                        env.getProperty("spring.application.name"),
                        protocol,
                        port,
                        contextPath,
                        protocol,
                        externalHost,
                        port,
                        contextPath
                )
        );
    }
}

