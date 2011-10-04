package v3.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Javadoc
 */
public abstract class Context
{
   public void reselectObjectsForRoles() throws Exception
   {
      for (Field roleField : getClass().getDeclaredFields())
      {
         // Only set "Role fields" beginning with upper case (we could use an annotation instead)
         if (!Character.isUpperCase( roleField.getName().charAt( 0 ) ))
            continue;

         try
         {
            Method roleBindingMethod = getClass().getDeclaredMethod( roleField.getName() );

            Object returnedRoleObject = roleBindingMethod.invoke( this );
            roleField.set( this, returnedRoleObject );
         }
         catch (NoSuchMethodException e)
         {
            throw new Exception( "Role field '" + roleField.getName() +
                  "' has no role binding method implemented in '" + this.getClass().getName() + "'" );
         }
      }
   }
}
