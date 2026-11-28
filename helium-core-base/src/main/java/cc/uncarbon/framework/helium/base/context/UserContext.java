package cc.uncarbon.framework.helium.base.context;

import java.util.Collection;

/**
 * 用户上下文
 *
 * @author Uncarbon
 */
public interface UserContext {

   String CAMEL_NAME = "userContext";


   /**
    * 取得用户ID
    */
   Long getUserId();

   /**
    * 取得用户名
    */
   String getUserPin();

   /**
    * 取得用户类型编码
    */
   String getUserTypeCode();

   /**
    * 取得用户角色ID集合
    */
   Collection<Long> getRoleIds();

   /**
    * 取得用户角色编码集合
    */
   Collection<String> getRoleCodes();

}
