package v03.behavior.context;

import v03.api.Context;
import v03.api.ContextMixin;
import v03.behavior.role.DestinationAccount;
import v03.behavior.role.SourceAccount;
import v03.domain.entity.CheckingAccountEntity;
import v03.domain.entity.SavingsAccountEntity;
import org.qi4j.api.mixin.Mixins;

/**
 * TransferMoney use case context
 */
@Mixins( TransferMoneyContext.Mixin.class )
public interface TransferMoneyContext
      extends Context
{
   void init( Integer amount, String savingsAccountId, String checkingAccountId );
   void run() throws Exception;

   abstract class Mixin
         extends ContextMixin
         implements TransferMoneyContext
   {
      Integer amount;
      SourceAccount sourceAccount;
      DestinationAccount destinationAccount;

      Integer initialSourceAccountBalance;
      Integer initialDestinationAccountBalance;

      public void init( Integer amount, String savingsAccountId, String checkingAccountId )
      {
         this.amount = amount;

         sourceAccount = rolePlayer( SourceAccount.class, SavingsAccountEntity.class, savingsAccountId );
         destinationAccount = rolePlayer( DestinationAccount.class, CheckingAccountEntity.class, checkingAccountId );

         initialSourceAccountBalance = sourceAccount.getBalance();
         initialDestinationAccountBalance = destinationAccount.getBalance();
      }

      public void run() throws Exception
      {
         sourceAccount.transfer( amount, destinationAccount );

         checkPostConditions();
      }

      // Explicitly check the post-conditions of the use case
      private void checkPostConditions() throws Exception
      {
         if (!sourceAccount.getBalance().equals( initialSourceAccountBalance - amount ))
            throw new Exception( "Source Account balance is now " + sourceAccount.getBalance()
                  + " - it should have been " + ( initialSourceAccountBalance - amount ) );

         if (!destinationAccount.getBalance().equals( amount ))
            throw new Exception( "Destination Account balance is now " + destinationAccount.getBalance()
                  + " - it should have been " + ( initialDestinationAccountBalance + amount ) );

         System.out.println( "Transfer completed successfully" );
      }
   }
}