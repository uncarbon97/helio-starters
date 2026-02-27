package cc.uncarbon.framework.helio.base.context;

import java.util.Collection;

public interface UserContext {

   String CAMEL_NAME = "userContext";


   /**
    * 取得用户ID
    */
   Long getUserId();

   /**
    * 取得用户名
    */
   String getUserName();

   /**
    * 取得用户类型编码
    */
   String getUserTypeCode();

   /**
    * 取得用户角色ID集合
    */
   Collection<Long> getRolesIds();

   /**
    * 取得用户角色编码集合
    */
   Collection<String> getRoleCodes();

}
