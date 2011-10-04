package v11.api;

import java.lang.reflect.Method;

/**
 * Context base class
 */
public abstract class Context
{
   public void reselectObjectsForRoles() throws Exception
   {
      for (Method method : getClass().getDeclaredMethods())
      {
         if (method.getAnnotation( RoleBinding.class ) != null)
         {
            method.invoke( this );
            return;
         }
      }
   }
}
