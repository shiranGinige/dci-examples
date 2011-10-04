package v01.behavior.context;

import v01.behavior.role.MoneySinkRole;
import v01.behavior.role.MoneySourceRole;
import v01.domain.entity.CheckingAccountEntity;
import v01.domain.entity.SavingsAccountEntity;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * Transfer Money use case context
 */
@Mixins( TransferMoneyContext.Mixin.class )
public interface TransferMoneyContext
      extends TransientComposite
{
   void init( Integer amount, String sourceId, String destId );
   void run() throws Exception;

   abstract class Mixin
         implements TransferMoneyContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      Integer amount;
      MoneySourceRole moneySourceRole;
      MoneySinkRole moneySinkRole;

      public void init( Integer amount, String sourceId, String destId )
      {
         this.amount = amount;

         SavingsAccountEntity savingsAccount = uowf.currentUnitOfWork().get( SavingsAccountEntity.class, sourceId );
         // Domain object is casted to a Role
         moneySourceRole = (MoneySourceRole) savingsAccount;

         CheckingAccountEntity checkingAccount = uowf.currentUnitOfWork().get( CheckingAccountEntity.class, destId );
         moneySinkRole = (MoneySinkRole) checkingAccount;
      }

      public void run() throws Exception
      {
         System.out.println( "Source: " + moneySourceRole.balance().get() + ", Sink: " + moneySinkRole.balance().get() );

         // Trigger method
         moneySourceRole.transfer( amount, moneySinkRole );

         System.out.println( "Source: " + moneySourceRole.balance().get() + ", Sink: " + moneySinkRole.balance().get() );
         System.out.println( "--------------------------" );
      }
   }
}