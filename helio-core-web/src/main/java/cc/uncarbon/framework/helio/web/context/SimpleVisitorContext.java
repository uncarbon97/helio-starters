package cc.uncarbon.framework.helio.web.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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


    @Schema(description = "IP 地址文本")
    protected String ip;

    @Schema(description = "浏览器 UA")
    protected String userAgent;

    @Schema(description = "HTTP 请求方式，如：GET、POST、PUT 等")
    protected String httpRequestMethod;

    @Schema(description = "HTTP 请求路径，不包含 schema、host、port 等部分")
    protected String httpRequestPath;

}
