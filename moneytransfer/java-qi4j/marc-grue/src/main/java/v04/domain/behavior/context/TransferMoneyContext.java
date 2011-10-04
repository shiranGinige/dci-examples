package v04.domain.behavior.context;

import v04.api.Context;
import v04.api.ContextMixin;
import v04.domain.behavior.role.DestinationAccount;
import v04.domain.behavior.role.SourceAccount;
import v04.domain.structure.entity.CheckingAccountEntity;
import v04.domain.structure.entity.SavingsAccountEntity;
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
   void run();

   abstract class Mixin
         extends ContextMixin
         implements TransferMoneyContext
   {
      Integer amount;
      SourceAccount sourceAccount;
      DestinationAccount destinationAccount;

      public void init( Integer amount, String savingsAccountId, String checkingAccountId )
      {
         this.amount = amount;
         sourceAccount = rolePlayer( SourceAccount.class, SavingsAccountEntity.class, savingsAccountId );
         destinationAccount = rolePlayer( DestinationAccount.class, CheckingAccountEntity.class, checkingAccountId );
      }

      // For passing in role objects      
      public void init( Integer amount, SourceAccount sourceAccount, DestinationAccount destinationAccount )
      {
         this.amount = amount;
         this.sourceAccount = sourceAccount;
         this.destinationAccount = destinationAccount;
      }

      public void run()
      {
         sourceAccount.transfer( amount, destinationAccount );
      }
   }
}