package v04.domain.behavior.role;

import v04.api.Role;
import v04.domain.behavior.context.TransferMoneyContext;
import v04.domain.structure.data.BalanceData;
import v04.domain.structure.entity.CreditorEntity;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Source account role
 */
@Mixins( SourceAccount.Mixin.class )
public interface SourceAccount
      extends Role, BalanceData
{
   void transfer( Integer amount, DestinationAccount destinationAccount );
   void payBills();

   public abstract class Mixin
         implements SourceAccount
   {
      @Structure
      TransientBuilderFactory tbf;

      @Structure
      UnitOfWorkFactory uowf;

      public void transfer( Integer amount, DestinationAccount destinationAccount )
      {
         printBalances( destinationAccount );

         decreaseBalance( amount );
         System.out.println( "Source account      -" + amount );
         destinationAccount.receive( amount );

         printBalances( destinationAccount );
      }

      public void payBills( )
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

      private void checkAvailableFundsForAllBills( List<DestinationAccount> creditors ) throws RuntimeException
      {
         Integer debt = 0;
         for (DestinationAccount creditor : creditors)
         {
            // Creditor playing the Role as DestinationAccount
            debt += creditor.getBalance();
            System.out.println( creditor + " balance: " + creditor.getBalance() );
         }
         Integer balanceInSavingsAccount = getBalance();
         System.out.println( "Creditor balance in total: " + debt );

         // abort if there's not enough money to pay all bills
         if (balanceInSavingsAccount + debt < 0)
            throw new RuntimeException( "Insufficient funds to pay bills." );
      }

      private void printBalances( DestinationAccount destinationAccount )
      {
         System.out.println( "Source account " + getBalance()
               + "  Destination account " + destinationAccount.getBalance() );
         System.out.println( "-------------------------------------------" );
      }
   }
}