package v02.behavior.role;

import v02.domain.data.SavingsAccountData;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.This;

/**
 * Source account role
 */
@Mixins( SourceAccount.Mixin.class )
public interface SourceAccount
      extends TransientComposite
{
   void transfer( Integer amount, DestinationAccount destinationAccount );

   public abstract class Mixin
         implements SourceAccount
   {
      @This
      SavingsAccountData data;

      public void transfer( Integer amount, DestinationAccount destinationAccount )
      {
         printBalances( destinationAccount );

         data.decreaseBalance( amount ); // SavingsAccountData method
         System.out.println( "Source account      -" + amount );
         destinationAccount.receive( amount );

         printBalances( destinationAccount );
      }

      private void printBalances( DestinationAccount destinationAccount )
      {
         System.out.println( "Source account " + data.balance().get() // Private "raw" data from SavingsAccount
               + "  Destination account " + destinationAccount.getBalance() );
         System.out.println( "-------------------------------------------" );
      }
   }
}