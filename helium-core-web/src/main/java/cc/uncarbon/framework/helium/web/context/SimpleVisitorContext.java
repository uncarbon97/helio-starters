package cc.uncarbon.framework.helium.web.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 简单访客上下文
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SimpleVisitorContext implements VisitorContext {


    @Schema(description = "客户端 IP 地址文本；用户的请求可能经过多次反向代理，取最靠近真实用户的那个")
    protected String clientIp;

    @Schema(description = "本次请求经过的所有 IP 地址文本；用户的请求可能经过多次反向代理")
    protected List<String> clientIpList;

    @Schema(description = "浏览器 UA")
    protected String userAgent;

    @Schema(description = "HTTP 请求方式，如：GET、POST、PUT 等")
    protected String httpRequestMethod;

    @Schema(description = "HTTP 请求路径，不包含 schema、host、port 等部分")
    protected String httpRequestPath;

}
