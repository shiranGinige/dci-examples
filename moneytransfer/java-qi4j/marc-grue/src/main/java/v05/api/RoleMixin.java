package v05.api;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * All Role Mixin's extend RoleMixin
 */

public abstract class RoleMixin
{
   @Structure
   public UnitOfWorkFactory uowf;

   @Structure
   public TransientBuilderFactory tbf;

   @Service
   public RoleMap roleMap;
}