package v04.api;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * Context mixin base class - provides helper functions for Context mixins
 */
public abstract class ContextMixin
{
   @Structure
   public TransientBuilderFactory tbf;

   @Structure
   public UnitOfWorkFactory uowf;

   protected <T extends Role, U extends DomainEntity> T rolePlayer(Class<T> roleClass, Class<U> entityClass, String entityId)
   {
      U entityObject = uowf.currentUnitOfWork().get( entityClass, entityId );

      if (roleClass.isAssignableFrom( entityObject.getClass() ))
      {
         return roleClass.cast(entityObject);
      }

      throw new IllegalArgumentException( "Entity '" + entityObject.getClass().getSimpleName() +
            "' can't play Role of '" + roleClass.getSimpleName() + "'" );
   }
}
