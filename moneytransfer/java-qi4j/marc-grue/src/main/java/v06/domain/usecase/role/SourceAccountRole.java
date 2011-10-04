package v06.domain.usecase.role;

import v06.api.Role;
import v06.api.RoleMixin;
import v06.domain.data.Account;
import v06.domain.data.Transactions;
import v06.domain.entity.AccountEntity;
import v06.domain.usecase.context.TransferMoneyContext;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWork;

import java.util.ArrayList;
import java.util.List;

/**
 * Source account role
 */
@Mixins( SourceAccountRole.Mixin.class )
public interface SourceAccountRole
      extends Role
{
   void transfer( Integer amount );
   void payBills();

   abstract class Mixin
         extends RoleMixin
         implements SourceAccountRole
   {
      private Transactions transactions;
      private String sourceAccountId;
      private Integer balance;

      @This
      Account sourceAccount;

      public void transfer( Integer amount )
      {
         init();
         
         printBalances();

         hasSufficientFunds( amount );
         withDraw( amount );
         System.out.println( "Source account      -" + amount );
         roleMap.get( DestinationAccountRole.class ).deposit( amount );

         printBalances();
      }

      public void payBills()
      {
         init();

         List<DestinationAccountRole> creditors = getCreditors();
         checkAvailableFundsForAllBills( creditors );

         for (DestinationAccountRole creditor : creditors)
         {
            payBillsTo( creditor );
         }
      }

      private void init()
      {
         transactions = roleMap.get( Transactions.class );
         sourceAccountId = EntityReference.getEntityReference( sourceAccount ).identity();
         balance = transactions.getBalance( sourceAccountId );
      }

      private boolean hasSufficientFunds( Integer amount ) throws IllegalStateException
      {
         if (amount < 0)
            throw new IllegalArgumentException( "Amount is expected to be a positive number" );

         if (balance.equals( sourceAccount.allowedMinimum().get() ))
            throw new IllegalArgumentException( "Credit limit is reached." );
         if (( balance - amount ) < sourceAccount.allowedMinimum().get())
            throw new IllegalArgumentException( "Insufficient funds. Available funds: "
                  + sourceAccount.allowedMinimum().get() + balance );

         return true;
      }

      private void withDraw( Integer amount )
      {
         transactions.createTransaction( "Transfer" );
         transactions.addTransactionEntry( sourceAccountId, -amount );
      }

      private List<DestinationAccountRole> getCreditors()
      {
         // "Database" request - could be its own use case...
         List<DestinationAccountRole> creditors = new ArrayList<DestinationAccountRole>();
         UnitOfWork uow = uowf.currentUnitOfWork();
         creditors.add( uow.get( AccountEntity.class, "BakerAccountId" ) );
         creditors.add( uow.get( AccountEntity.class, "ButcherAccountId" ) );
         return creditors;
      }

      private void checkAvailableFundsForAllBills( List<DestinationAccountRole> creditors ) throws IllegalStateException
      {
         Integer debt = 0;
         for (DestinationAccountRole creditor : creditors)
         {
            String creditorAccountId = EntityReference.getEntityReference( creditor ).identity();
            Integer creditorBalance = transactions.getBalance( creditorAccountId );
            debt += creditorBalance;
            System.out.println( creditor + " balance: " + creditorBalance );
         }
         System.out.println( "Creditor debt in total: " + debt );

         // abort if there's not enough money to pay all bills
         if (balance + debt < sourceAccount.allowedMinimum().get())
            throw new RuntimeException( "Insufficient funds to pay bills. Balance=" + balance
                  + " Debt=" + debt + " Allowed minimum=" + sourceAccount.allowedMinimum().get() );
      }

      private void payBillsTo( DestinationAccountRole creditor )
      {
         String creditorAccountId = EntityReference.getEntityReference( creditor ).identity();
         Integer creditorBalance = transactions.getBalance( creditorAccountId );
         if (creditorBalance < 0)
         {
            TransferMoneyContext transferMoneyContext = tbf.newTransient( TransferMoneyContext.class );
            transferMoneyContext.transfer( -creditorBalance, this, creditor, transactions );
         }
      }

      private void printBalances()
      {
         Account destinationAccount = (Account) roleMap.get( DestinationAccountRole.class );
         String destinationAccountId = EntityReference.getEntityReference( destinationAccount ).identity();

         System.out.println( "Source account " + transactions.getBalance( sourceAccountId )
               + "  Destination account " + transactions.getBalance( destinationAccountId ) );
         System.out.println( "-------------------------------------------" );
      }
   }
}