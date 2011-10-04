package v05.api;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
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

   @Service
   public RoleMap roleMap;

   protected <T extends Role, U extends DomainEntity> void addRolePlayer( Class<T> roleClass, Class<U> entityClass, String entityId )
   {
      U entityObject = uowf.currentUnitOfWork().get( entityClass, entityId );
      roleMap.set( entityObject, roleClass );
   }

   protected <T extends Role> void addRolePlayer( T roleObject )
   {
      roleMap.set( roleObject );
   }
}