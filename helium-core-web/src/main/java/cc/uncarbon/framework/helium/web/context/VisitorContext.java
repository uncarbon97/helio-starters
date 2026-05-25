package cc.uncarbon.framework.helium.web.context;

/**
 * 访客上下文
 * 与 {@link cc.uncarbon.framework.helium.base.context.UserContext} 的区别是：
 * 1. 保存了 IP 地址
 * 2. 记录了匿名访问时的访问者信息
 *
 * @author Uncarbon
 */
public interface VisitorContext {

   String CAMEL_NAME = "visitorContext";


   /**
    * 取得 IP 地址文本
    */
   String getIp();

   /**
    * 取得浏览器 UA
    */
   String getUserAgent();

   /**
    * 取得 HTTP 请求方式，如：GET、POST、PUT 等
    */
   String getHttpRequestMethod();

   /**
    * 取得 HTTP 请求路径，不包含 schema、host、port 等部分
    */
   String getHttpRequestPath();

}
