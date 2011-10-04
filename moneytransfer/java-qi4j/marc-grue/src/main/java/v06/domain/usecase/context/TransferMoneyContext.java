package v06.domain.usecase.context;

import v06.api.Context;
import v06.api.ContextMixin;
import v06.domain.data.Transactions;
import v06.domain.entity.AccountEntity;
import v06.domain.entity.TransactionsEntity;
import v06.domain.usecase.role.DestinationAccountRole;
import v06.domain.usecase.role.SourceAccountRole;
import org.qi4j.api.mixin.Mixins;

/**
 * TransferMoney use case context
 */
@Mixins( TransferMoneyContext.Mixin.class )
public interface TransferMoneyContext
      extends Context
{
   // More generic account ids pointing to unified AccountEntity
   void transfer( Integer amount, String sourceAccountId, String destinationAccountId );
   void transfer( Integer amount,
                  SourceAccountRole sourceAccountRole,
                  DestinationAccountRole destinationAccountRole,
                  Transactions transactions );

   abstract class Mixin
         extends ContextMixin
         implements TransferMoneyContext
   {
      // Use case enactment in one method
      public void transfer( Integer amount, String sourceAccountId, String destinationAccountId )
      {
         addRolePlayer( SourceAccountRole.class, AccountEntity.class, sourceAccountId );
         addRolePlayer( DestinationAccountRole.class, AccountEntity.class, destinationAccountId );

         // Adding a Data object to the roleMap - is this a sin or is it a Role?
         addRolePlayer( Transactions.class, TransactionsEntity.TRANSACTIONS );

         roleMap.get( SourceAccountRole.class ).transfer( amount );
      }

      // Passing role playing objects (used in PayBills use case)
      public void transfer( Integer amount,
                            SourceAccountRole sourceAccount,
                            DestinationAccountRole destinationAccount,
                            Transactions transactions
      )
      {
         addRolePlayer( sourceAccount );
         addRolePlayer( destinationAccount );
         addRolePlayer( transactions );

         roleMap.get( SourceAccountRole.class ).transfer( amount );
      }
   }
}