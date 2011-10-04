package v04.domain.behavior.context;

import v04.api.Context;
import v04.api.ContextMixin;
import v04.domain.behavior.role.SourceAccount;
import v04.domain.structure.entity.CheckingAccountEntity;
import org.qi4j.api.mixin.Mixins;

/**
 * PayBills use case
 * <p/>
 * 1. @AccountHolder selects @SourceAccount and requests to #payBills
 * - System displays the selected @SourceAccount
 * - System finds the list of @Creditors and amount owed to each
 * 2. @AccountHolder #accepts paying all bills
 * 3. @Bank
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
      SourceAccount sourceAccount;

      public void init( String checkingAccountId )
      {
         // CheckingAccount now plays a SourceAccount Role
         sourceAccount = rolePlayer( SourceAccount.class, CheckingAccountEntity.class, checkingAccountId );
      }

      public void run() throws Exception
      {
         sourceAccount.payBills( );

         checkPostConditions();
      }

      private void checkPostConditions() throws Exception
      {
         Integer initialSourceAccountBalance = 800;
         Integer debt = 140;
         Integer expectedBalance = initialSourceAccountBalance - debt;
         Integer finalBalance = sourceAccount.getBalance();

         if (!finalBalance.equals( expectedBalance ))
            throw new Exception( "Source Account balance is now " + finalBalance
                  + " - it should have been " + ( expectedBalance ) );
         System.out.println( "All bills successfully paid" );
      }
   }
}