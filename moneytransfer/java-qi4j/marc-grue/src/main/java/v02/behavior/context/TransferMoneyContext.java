package v02.behavior.context;

import v02.behavior.role.DestinationAccount;
import v02.behavior.role.SourceAccount;
import v02.domain.entity.CheckingAccountEntity;
import v02.domain.entity.SavingsAccountEntity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.composite.TransientComposite;

/**
 * TransferMoney use case context
 */
@Mixins( TransferMoneyContext.Mixin.class )
public interface TransferMoneyContext
      extends TransientComposite
{
   void init( Integer amount, String savingsAccountId, String checkingAccountId );
   void run() throws Exception;

   abstract class Mixin
         implements TransferMoneyContext
   {
      @Structure
      UnitOfWorkFactory uowf;

      Integer amount;
      SourceAccount sourceAccount;
      DestinationAccount destinationAccount;

      public void init( Integer amount, String savingsAccountId, String checkingAccountId )
      {
         this.amount = amount;

         SavingsAccountEntity savingsAccount = uowf.currentUnitOfWork().get( SavingsAccountEntity.class, savingsAccountId );
         // Domain object is casted to a Role
         sourceAccount = (SourceAccount) savingsAccount;

         CheckingAccountEntity checkingAccount = uowf.currentUnitOfWork().get( CheckingAccountEntity.class, checkingAccountId );
         destinationAccount = (DestinationAccount) checkingAccount;
      }

      public void run() throws Exception
      {
         sourceAccount.transfer( amount, destinationAccount );
      }
   }
}