package v04.domain.behavior.role;

import v04.api.Role;
import v04.domain.structure.data.BalanceData;
import org.qi4j.api.mixin.Mixins;

/**
 * Destination account Role
 * - contract with the Entity of what Data the Role can use
 * - Only Data defined in Entity is available as an option
 */
@Mixins( DestinationAccount.Mixin.class )
public interface DestinationAccount
      extends Role, BalanceData
{
   void receive( Integer amount );

   abstract class Mixin
         implements DestinationAccount
   {
      public void receive( Integer amount )
      {
         increaseBalance( amount );
         System.out.println( "Destination account +" + amount );
      }
   }
}
