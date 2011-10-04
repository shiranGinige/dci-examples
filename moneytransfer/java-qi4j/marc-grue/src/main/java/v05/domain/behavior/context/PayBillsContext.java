package v05.domain.behavior.context;

import v05.api.Context;
import v05.api.ContextMixin;
import v05.domain.behavior.role.SourceAccount;
import v05.domain.structure.entity.CheckingAccountEntity;
import org.qi4j.api.mixin.Mixins;

/**
 * Pay Bills use case
 */
@Mixins( PayBillsContext.Mixin.class )
public interface PayBillsContext
      extends Context
{
   void init( String checkingAccountId );
   void run() throws Exception;

   abstract class Mixin
         extends ContextMixin
         implements PayBillsContext
   {
      public void init( String checkingAccountId )
      {
         addRolePlayer( SourceAccount.class, CheckingAccountEntity.class, checkingAccountId );
      }

      public void run() throws Exception
      {
         roleMap.get( SourceAccount.class ).payBills();
      }
   }
}