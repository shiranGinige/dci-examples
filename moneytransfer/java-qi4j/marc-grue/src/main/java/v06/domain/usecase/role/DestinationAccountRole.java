package v06.domain.usecase.role;

import v06.api.Role;
import v06.api.RoleMixin;
import v06.domain.data.Account;
import v06.domain.data.Transactions;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

/**
 * Destination account Role
 */
@Mixins( DestinationAccountRole.Mixin.class )
public interface DestinationAccountRole
      extends Role
{
   public void deposit( Integer amount );

   abstract class Mixin
         extends RoleMixin
         implements DestinationAccountRole
   {
      @This
      Account destinationAccount;

      public void deposit( Integer amount )
      {
         Transactions transactions = roleMap.get( Transactions.class );
         String destinationAccountId = EntityReference.getEntityReference( destinationAccount ).identity();
         transactions.addTransactionEntry( destinationAccountId, amount );

         transactions.completeTransaction();

         System.out.println( "Destination account +" + amount );
      }
   }
}
