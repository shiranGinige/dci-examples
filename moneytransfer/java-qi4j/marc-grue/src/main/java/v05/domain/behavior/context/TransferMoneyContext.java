package v05.domain.behavior.context;

import v05.api.Context;
import v05.api.ContextMixin;
import v05.domain.behavior.role.DestinationAccount;
import v05.domain.behavior.role.SourceAccount;
import v05.domain.structure.entity.CheckingAccountEntity;
import v05.domain.structure.entity.SavingsAccountEntity;
import org.qi4j.api.mixin.Mixins;

/**
 * TransferMoney use case context
 */
@Mixins( TransferMoneyContext.Mixin.class )
public interface TransferMoneyContext
      extends Context
{
   void init( Integer amount, String savingsAccountId, String checkingAccountId );
   void init( Integer amount, SourceAccount sourceAccount, DestinationAccount destinationAccount );
   void run() throws Exception;

   abstract class Mixin
         extends ContextMixin
         implements TransferMoneyContext
   {
      Integer amount;

      public void init( Integer amount, String savingsAccountId, String checkingAccountId )
      {
         this.amount = amount;

         addRolePlayer( SourceAccount.class, SavingsAccountEntity.class, savingsAccountId );
         addRolePlayer( DestinationAccount.class, CheckingAccountEntity.class, checkingAccountId );
      }

      public void init( Integer amount, SourceAccount sourceAccount, DestinationAccount destinationAccount )
      {
         this.amount = amount;

         addRolePlayer( sourceAccount );
         addRolePlayer( destinationAccount );
      }

      public void run() throws Exception
      {
         // destinationAccount is *not* passed (is in roleMap)
         roleMap.get( SourceAccount.class ).transfer( amount );
      }
   }
}