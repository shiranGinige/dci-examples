package v03.behavior.role;

import v03.api.Role;
import v03.domain.data.BalanceData;
import org.qi4j.api.mixin.Mixins;

/**
 * Destination account Role
 */
@Mixins( DestinationAccount.Mixin.class )
public interface DestinationAccount
      extends Role
{
   void receive( Integer amount );

   Integer getBalance();

   abstract class Mixin
         implements DestinationAccount, BalanceData
   {
      public void receive( Integer amount )
      {
         increaseBalance( amount );
         System.out.println( "Destination account +" + amount );
      }

      public Integer getBalance()
      {
         return balance().get();
      }
   }
}