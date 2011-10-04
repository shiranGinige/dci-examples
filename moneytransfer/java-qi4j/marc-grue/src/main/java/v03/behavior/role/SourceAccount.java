package v03.behavior.role;

import v03.api.Role;
import v03.domain.data.BalanceData;
import org.qi4j.api.mixin.Mixins;

/**
 * Source account role
 */
@Mixins( SourceAccount.Mixin.class )
public interface SourceAccount
      extends Role
{
   void transfer( Integer amount, DestinationAccount destinationAccount );

   // Allowed Data access
   Integer getBalance();

   public abstract class Mixin
         implements SourceAccount, BalanceData
   {
      public void transfer( Integer amount, DestinationAccount destinationAccount )
      {
         printBalances( destinationAccount );

         decreaseBalance( amount ); // BalanceData method
         System.out.println( "Source account      -" + amount );
         destinationAccount.receive( amount );

         printBalances( destinationAccount );
      }

      public Integer getBalance()
      {
         return balance().get(); // Property value from BalanceData
      }

      private void printBalances( DestinationAccount destinationAccount )
      {
         System.out.println( "Source account " + balance().get()
               + "  Destination account " + destinationAccount.getBalance() );
         System.out.println( "-------------------------------------------" );
      }
   }
}