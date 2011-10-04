package v06.api;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * Javadoc
 */
public abstract class ContextMixin
{
   @Structure
   public TransientBuilderFactory tbf;

   @Structure
   public UnitOfWorkFactory uowf;

   @Service
   public RoleMap roleMap;

   protected <T, U extends DomainEntity> void addRolePlayer( Class<T> roleClass, Class<U> entityClass, String entityId )
   {
      U entityObject = uowf.currentUnitOfWork().get( entityClass, entityId );
      roleMap.set( entityObject, roleClass );
   }
   
   protected <U> void addRolePlayer( Class<U> dataClass, String entityId )
   {
      U entityObject = uowf.currentUnitOfWork().get( dataClass, entityId );
      roleMap.set( entityObject );
   }

   protected <T> void addRolePlayer( T roleObject )
   {
      roleMap.set( roleObject );
   }
}