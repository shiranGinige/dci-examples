package v05.domain.behavior.role;

import v05.api.Role;
import v05.api.RoleMixin;
import v05.domain.behavior.context.TransferMoneyContext;
import v05.domain.structure.data.BalanceData;
import v05.domain.structure.entity.CreditorEntity;
import org.qi4j.api.mixin.Mixins;

import java.util.ArrayList;
import java.util.List;

/**
 * Source account role
 */
@Mixins( SourceAccount.Mixin.class )
public interface SourceAccount
      extends Role, BalanceData
{
   // Interactions
   void transfer( Integer amount );
   void payBills() throws Exception;

   abstract class Mixin
         extends RoleMixin
         implements SourceAccount
   {
      public void transfer( Integer amount )
      {
         printBalances();

         decreaseBalance( amount );
         System.out.println( "Source account      -" + amount );
         roleMap.get( DestinationAccount.class ).receive( amount );

         printBalances();
      }

      public void payBills() throws Exception
      {
         List<DestinationAccount> creditors = getCreditors();

         checkAvailableFundsForAllBills( creditors );

         // pay the bills, one by one
         for (DestinationAccount creditor : creditors)
         {
            if (creditor.getBalance() < 0)
            {
               TransferMoneyContext transferMoneyContext = tbf.newTransient( TransferMoneyContext.class );
               transferMoneyContext.init( -creditor.getBalance(), this, creditor );
               transferMoneyContext.run();
            }
         }
      }

      private List<DestinationAccount> getCreditors()
      {
         // Creditor retrieval would be a use case in itself...
         List<DestinationAccount> creditors = new ArrayList<DestinationAccount>();
         creditors.add( uowf.currentUnitOfWork().get( CreditorEntity.class, "BakerAccountId" ) );
         creditors.add( uowf.currentUnitOfWork().get( CreditorEntity.class, "ButcherAccountId" ) );
         return creditors;
      }

      private void checkAvailableFundsForAllBills( List<DestinationAccount> creditors ) throws IllegalStateException
      {
         Integer debt = 0;
         for (DestinationAccount creditor : creditors)
         {
            // Creditor playing the Role of DestinationAccount
            debt += creditor.getBalance();
            System.out.println( creditor + " balance: " + creditor.getBalance() );
         }
         Integer balanceInSavingsAccount = getBalance();
         System.out.println( "Creditor debt in total: " + debt );

         // abort if there's not enough money to pay all bills
         if (balanceInSavingsAccount + debt < 0)
            throw new RuntimeException( "Insufficient funds to pay bills." );
      }

      private void printBalances()
      {
         System.out.println( "Source account " + getBalance()
               + "  Destination account " + roleMap.get( DestinationAccount.class ).getBalance() );
         System.out.println( "-------------------------------------------" );
      }
   }
}